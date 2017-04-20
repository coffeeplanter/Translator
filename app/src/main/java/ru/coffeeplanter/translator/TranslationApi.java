package ru.coffeeplanter.translator;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Интерфейс для HTTP-запроса через Retrofit.
 */

public interface TranslationApi {

    @GET("api/v1.5/tr.json/getLangs")
    Call<JsonObject> getLanguages(
            @Query("ui") String userLanguage,
            @Query("key") String key
    );

    @GET("api/v1.5/tr.json/translate")
    Call<JsonObject> getTranslation(
            @Query("text") String textToTranslate,
            @Query("lang") String translationDirection,
            @Query("key") String key
    );

}
