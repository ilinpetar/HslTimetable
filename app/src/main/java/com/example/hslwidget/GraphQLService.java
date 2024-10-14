package com.example.hslwidget;

import com.google.gson.Gson;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface GraphQLService {
    @Headers({"Content-Type: application/graphql"})
    @POST("routing/v1/routers/hsl/index/graphql")
    Call<Map<String, Object>> obtainTimetables(@Query("digitransit-subscription-key") String key, @Body String query);
}
