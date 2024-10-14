package com.example.hslwidget;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface GraphQLService {

    record Trip(String routeShortName) {}
    record StopTime(Long realtimeArrival, String headsign, Trip trip) {}
    record Stop(String name, List<StopTime> stoptimesWithoutPatterns) {}
    record Data(List<Stop> stops) {}
    record HslResponse(Data data) {}

    @Headers({"Content-Type: application/graphql"})
    @POST("routing/v1/routers/hsl/index/graphql")
    Call<HslResponse> obtainTimetables(@Query("digitransit-subscription-key") String key, @Body String query);
}
