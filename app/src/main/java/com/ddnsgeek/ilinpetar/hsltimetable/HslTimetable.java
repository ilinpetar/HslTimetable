package com.ddnsgeek.ilinpetar.hsltimetable;

import android.content.Context;
import android.icu.text.DateFormat;
import android.text.Html;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.widget.TextView;
import androidx.core.text.HtmlCompat;
import androidx.preference.PreferenceManager;
import com.ddnsgeek.ilinpetar.hsltimetable.GraphQLService.HslResponse;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
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

    private final Context context;
    private Set<String> routes;
    private final String graphqlQuery = """
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

    public HslTimetable(Context context) {
        this.context = context;
    }

    public void obtainTimetables(TextView view) {

        var preferences = PreferenceManager.getDefaultSharedPreferences(context);
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
        Call<HslResponse> call = service.obtainTimetables(subscriptionKey, graphqlQuery.formatted(stopsArray));
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NotNull Call<HslResponse> call, @NotNull Response<HslResponse> response) {
                view.setText(processData(response.body()));
            }

            @Override
            public void onFailure(@NotNull Call<HslResponse> call, @NotNull Throwable t) {
                // Handle the error
                StringBuilder buffer = new StringBuilder();
                buffer.append("<h1>")
                    .append("Last update: ").append(DateFormat.getTimeInstance(DateFormat.MEDIUM, Locale.UK).format(new Date()))
                    .append("</h1>")
                    .append("<font color='red'> ")
                    .append(t.getLocalizedMessage())
                    .append("</font>");
                view.setText(Html.fromHtml(buffer.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY));
            }
        });
    }

    private Spanned processData(HslResponse hslResponse) {
        var now = LocalTime.now();
        var secondOfDay = now.get(ChronoField.SECOND_OF_DAY);

        StringBuilder buffer = new StringBuilder();
        buffer.append("<h1>")
            .append("Last update: ").append(DateFormat.getTimeInstance(DateFormat.MEDIUM, Locale.UK).format(new Date()))
            .append("</h1>");

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
                        .append("in ")
                        .append(DateUtils.formatElapsedTime(arrival))
                        .append(" min:sec")
                        .append("</font>")
                        .append("</b>")
                        .append("</div>");
                }
            });
        });
        return Html.fromHtml(buffer.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY);
    }
}
