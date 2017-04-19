package ru.coffeeplanter.translator;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Интерфейс для Ретрофита.
 */

public interface TranslationApi {

    @GET("api/v1.5/tr.json/getLangs")
    Call<JsonObject> getData(@Query("ui") String resourceName, @Query("key") String key);

}
