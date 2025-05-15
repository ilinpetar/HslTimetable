package com.ddnsgeek.ilinpetar.hsltimetable.service;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface GraphQLService {

    record Trip(String routeShortName, String directionId) {

    }

    record StopTime(Long realtimeArrival, String headsign, Trip trip) {

    }

    record Stop(String name, String code, List<StopTime> stoptimesWithoutPatterns) {

    }

    record Data(List<Stop> stops) {

    }

    record HslResponse(Data data) {

    }

    @Headers({"Content-Type: application/graphql"})
    @POST("routing/v2/hsl/gtfs/v1")
    Call<HslResponse> obtainTimetables(@Query("digitransit-subscription-key") String key, @Body String query);
}
