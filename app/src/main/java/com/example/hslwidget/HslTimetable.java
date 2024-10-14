package com.example.hslwidget;

import android.icu.text.DateFormat;
import android.text.Html;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.widget.TextView;
import androidx.core.text.HtmlCompat;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
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

    private final String graphqlQuery = """
        query busTimetable($timeRange: Int = 120000) {
          Matinsolmu: stop(id: "HSL:2232280") {
            name
            BUS164: stopTimesForPattern(id: "HSL:2164:1:01", timeRange: $timeRange) {
              realtimeArrival
            }
            BUS164K: stopTimesForPattern(id: "HSL:2164K:1:01", timeRange: $timeRange) {
              realtimeArrival
            }
          }
          Niittykummunsilta: stop(id: "HSL:2322229") {
            name
            BUS121: stopTimesForPattern(id: "HSL:2121:1:01", timeRange: $timeRange) {
              realtimeArrival
            }
            BUS121A: stopTimesForPattern(id: "HSL:2121A:1:01", timeRange: $timeRange) {
              realtimeArrival
            }
          }
          Lansivayla: stop(id: "HSL:1201229") {
            name
            BUS164: stopTimesForPattern(id: "HSL:2164:0:01", timeRange: $timeRange) {
              realtimeArrival
            }
            BUS164K: stopTimesForPattern(id: "HSL:2164K:0:01", timeRange: $timeRange) {
              realtimeArrival
            }
            BUS121: stopTimesForPattern(id: "HSL:2121:0:01", timeRange: $timeRange) {
              realtimeArrival
            }
            BUS121A: stopTimesForPattern(id: "HSL:2121A:0:01", timeRange: $timeRange) {
              realtimeArrival
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
        Call<Map<String, Object>> call = service.obtainTimetables(subscriptionKey, graphqlQuery);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                Map<String, Object> timetables = response.body();
                view.setText(processData(timetables));
                // Handle the list of users
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                // Handle the error
            }
        });
    }

    private Spanned processData(Map<String, Object> timetables) {
        var now = LocalTime.now();
        var secondOfDay = now.get(ChronoField.SECOND_OF_DAY);
        var data = (Map<String, Object>) timetables.get("data");
        var matinsolmu = (Map<String, Object>) data.get("Matinsolmu");
        var niittykummunsilta = (Map<String, Object>) data.get("Niittykummunsilta");
        var lansivayla = (Map<String, Object>) data.get("Lansivayla");

        var matinsolmu_164 = (List<Map<String, Double>>) matinsolmu.get("BUS164");
        var matinsolmu_164K = (List<Map<String, Double>>) matinsolmu.get("BUS164K");
        var niittykummunsilta_121 = (List<Map<String, Double>>) niittykummunsilta.get("BUS121");
        var niittykummunsilta_121A = (List<Map<String, Double>>) niittykummunsilta.get("BUS121A");
        var lansivayla_164 = (List<Map<String, Double>>) lansivayla.get("BUS164");
        var lansivayla_164K = (List<Map<String, Double>>) lansivayla.get("BUS164K");
        var lansivayla_121 = (List<Map<String, Double>>) lansivayla.get("BUS121");
        var lansivayla_121A = (List<Map<String, Double>>) lansivayla.get("BUS121A");

        StringBuffer buffer = new StringBuffer();
        buffer.append("<h2 style=\"color:red;\">");
        buffer.append("Last update: ").append(DateFormat.getTimeInstance(DateFormat.MEDIUM).format(new Date()));
        buffer.append("</h2>");

        if (secondOfDay < 36000) {
            buffer.append("\n\nMatinsolmu:\n");
            buffer.append("\t-164\n");
            formatTime(matinsolmu_164, secondOfDay, buffer);
            buffer.append("\t-164K\n");
            formatTime(matinsolmu_164K, secondOfDay, buffer);
            buffer.append("\n\nNiittykummunsilta:\n");
            buffer.append("\t-121\n");
            formatTime(niittykummunsilta_121, secondOfDay, buffer);
            buffer.append("\t-121A\n");
            formatTime(niittykummunsilta_121A, secondOfDay, buffer);
        } else {
            buffer.append("\n\nLänsiväylä:\n");
            buffer.append("\t-164\n");
            formatTime(lansivayla_164, secondOfDay, buffer);
            buffer.append("\t-164K\n");
            formatTime(lansivayla_164K, secondOfDay, buffer);
            buffer.append("\t-121\n");
            formatTime(lansivayla_121, secondOfDay, buffer);
            buffer.append("\t-121A\n");
            formatTime(lansivayla_121A, secondOfDay, buffer);
        }
        return Html.fromHtml(buffer.toString(), HtmlCompat.FROM_HTML_MODE_COMPACT);
    }

    private void formatTime(List<Map<String, Double>> list, int secondOfDay, StringBuffer buffer) {
        if (list == null) {
            return;
        }
        list.forEach(elem -> {
            if (elem.containsKey("realtimeArrival")) {
                buffer.append("\t\t")
                    .append(DateUtils.formatElapsedTime(elem.get("realtimeArrival").longValue() - secondOfDay))
                    .append("\n");
            }
        });
    }
}
