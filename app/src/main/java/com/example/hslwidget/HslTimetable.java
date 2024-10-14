package com.example.hslwidget;

import android.icu.text.DateFormat;
import android.text.Html;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.widget.TextView;
import androidx.core.text.HtmlCompat;
import com.example.hslwidget.GraphQLService.HslResponse;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class HslTimetable {

    private final String hslGraphqlBaseUrl = "https://api.digitransit.fi/";
    private final String subscriptionKey = "76cc1b419f644891bee2588cfbaf7b5e";
    private final List<String> routeShortNames = Arrays.asList("121", "121A", "164", "164K");
    private final String graphqlQuery = """
        {
          stops(ids: ["HSL:2232280","HSL:2322229","HSL:1201229"]) {
            name
            stoptimesWithoutPatterns(timeRange: 1800, numberOfDepartures: 10) {
              realtimeArrival
              headsign
              trip {
                routeShortName
              }
            }
          }
        }
        """;

    public void obtainTimetables(TextView view) {
        var retrofit = new Retrofit.Builder()
            .baseUrl(hslGraphqlBaseUrl)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build();
        var service = retrofit.create(GraphQLService.class);
        Call<HslResponse> call = service.obtainTimetables(subscriptionKey, graphqlQuery);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<HslResponse> call, Response<HslResponse> response) {
                var hslResponse = response.body();
                view.setText(processData(hslResponse));
            }

            @Override
            public void onFailure(Call<HslResponse> call, Throwable t) {
                // Handle the error
                StringBuilder buffer = new StringBuilder();
                buffer.append("<h1>")
                    .append("Last update: ").append(DateFormat.getTimeInstance(DateFormat.MEDIUM).format(new Date()))
                    .append("</h1>")
                    .append(t.getLocalizedMessage());
                view.setText(Html.fromHtml(buffer.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY));
            }
        });
    }

    private Spanned processData(HslResponse hslResponse) {
        var now = LocalTime.now();
        var secondOfDay = now.get(ChronoField.SECOND_OF_DAY);

        StringBuilder buffer = new StringBuilder();
        buffer.append("<h1>")
            .append("Last update: ").append(DateFormat.getTimeInstance(DateFormat.MEDIUM).format(new Date()))
            .append("</h1>");

        hslResponse.data().stops().forEach(stop -> {
            buffer.append("<h3>")
                .append(stop.name())
                .append("</h3>");

            stop.stoptimesWithoutPatterns().forEach(stopTime -> {
                if (routeShortNames.contains(stopTime.trip().routeShortName())) {
                    buffer.append("<div>")
                        .append("<b>")
                        .append(stopTime.trip().routeShortName())
                        .append("</b>")
                        .append(" [").append(stopTime.headsign()).append("] ")
                        .append("<b")
                        .append(DateUtils.formatElapsedTime(stopTime.realtimeArrival() - secondOfDay))
                        .append("</b>")
                        .append("</div>");
                }
            });
        });
        return Html.fromHtml(buffer.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY);
    }
}
