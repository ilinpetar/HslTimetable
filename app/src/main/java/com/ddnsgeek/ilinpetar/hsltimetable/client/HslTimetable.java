package com.ddnsgeek.ilinpetar.hsltimetable.client;

import static android.text.format.DateUtils.FORMAT_ABBREV_RELATIVE;
import static android.text.format.DateUtils.MINUTE_IN_MILLIS;

import android.Manifest.permission;
import android.content.pm.PackageManager;
import android.text.Html;
import android.text.format.DateUtils;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.preference.PreferenceManager;
import com.ddnsgeek.ilinpetar.hsltimetable.service.GraphQLService;
import com.ddnsgeek.ilinpetar.hsltimetable.service.GraphQLService.HslResponse;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class HslTimetable {

    private final AppCompatActivity activity;
    private Set<String> routes;
    private static final String GRAPHQL_QUERY = """
        {
          stops(ids: [%s]) {
            name
            code
            stoptimesWithoutPatterns(timeRange: 1800, numberOfDepartures: 10) {
              realtimeArrival
              headsign
              trip {
                routeShortName
                directionId
              }
            }
          }
        }
        """;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int NOTIFICATION_ID = 1;

    public HslTimetable(AppCompatActivity activity) {
        this.activity = activity;
    }

    public void obtainTimetables(TextView textView, NotificationCompat.Builder builder) {

        var preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        if (preferences.getAll().isEmpty()) {
            return;
        }
        routes = preferences.getStringSet("multi_select_routes_preference", new HashSet<>());
        var stops = preferences.getStringSet("multi_select_stops_preference", new HashSet<>());

        var retrofit = new Retrofit.Builder()
            .baseUrl("https://api.digitransit.fi/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build();
        var service = retrofit.create(GraphQLService.class);
        var subscriptionKey = preferences.getString("subscription_key", "");
        var stopsArray = stops.stream().map(stop -> String.format("\"%s\"", stop)).collect(Collectors.joining(","));
        Call<HslResponse> call = service.obtainTimetables(subscriptionKey, GRAPHQL_QUERY.formatted(stopsArray));
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NotNull Call<HslResponse> call, @NotNull Response<HslResponse> response) {
                if (response.body() != null) {
                    var textViewContent = generateTextViewContent(response.body());
                    textView.setText(Html.fromHtml(textViewContent, HtmlCompat.FROM_HTML_MODE_LEGACY));
                    var notificationContent = generateNotificationContent(response.body());
                    builder
                        .setContentText(notificationContent)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationContent));

                    if (ContextCompat.checkSelfPermission(activity, permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(activity, new String[]{permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
                    } else {
                        NotificationManagerCompat.from(activity).notify(NOTIFICATION_ID, builder.build());
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<HslResponse> call, @NotNull Throwable t) {
                // Handle the error
                String buffer = "<font color='red'>"
                    + t.getLocalizedMessage()
                    + "</font>";
                textView.setText(Html.fromHtml(buffer, HtmlCompat.FROM_HTML_MODE_LEGACY));
            }
        });
    }

    private String generateTextViewContent(HslResponse hslResponse) {
        var now = LocalTime.now();
        var secondOfDay = now.get(ChronoField.SECOND_OF_DAY);

        StringBuilder buffer = new StringBuilder();
        hslResponse.data().stops().forEach(stop -> {
            buffer.append("<h3>")
                .append(stop.name())
                .append(" ")
                .append(stop.code())
                .append("</h3>");

            stop.stoptimesWithoutPatterns().forEach(stopTime -> {
                // render only selected routes
                if (routes.contains(stopTime.trip().routeShortName())) {
                    long arrival = stopTime.realtimeArrival() - secondOfDay;
                    String color = arrival < 300 ? "#FF0000" : "#006400";
                    buffer.append("<div>")
                        .append("<b>")
                        .append(stopTime.trip().routeShortName())
                        .append("</b>")
                        .append(" [").append(stopTime.headsign()).append("] ")
                        .append("<b>")
                        .append("<font color='").append(color).append("'>")
                        .append(DateUtils.getRelativeTimeSpanString(stopTime.realtimeArrival() * 1000L, secondOfDay * 1000L, MINUTE_IN_MILLIS,
                            FORMAT_ABBREV_RELATIVE))
                        .append("</font>")
                        .append("</b>")
                        .append("</div>");
                }
            });
        });
        return buffer.toString();
    }

    private String generateNotificationContent(HslResponse hslResponse) {
        var now = LocalTime.now();
        var secondOfDay = now.get(ChronoField.SECOND_OF_DAY);

        StringBuilder buffer = new StringBuilder();

        hslResponse.data().stops().forEach(stop ->
            stop.stoptimesWithoutPatterns().forEach(stopTime -> {
                // render only selected routes
                if (routes.contains(stopTime.trip().routeShortName())) {
                    long arrival = stopTime.realtimeArrival() - secondOfDay;
                    if (arrival > 900) {
                        return;
                    }
                    buffer.append("•")
                        .append(stop.name().substring(0, 5))
                        .append("…")
                        .append(stopTime.trip().routeShortName())
                        .append("-")
                        .append(LocalTime.ofSecondOfDay(stopTime.realtimeArrival()).format(DateTimeFormatter.ofPattern("HH:mm")))
                        .append("\n");
                }
            }));
        return buffer.toString();
    }
}
