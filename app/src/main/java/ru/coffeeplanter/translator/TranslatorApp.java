package ru.coffeeplanter.translator;

import android.app.Application;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Класс приложения, где инициализируются и хранятся
 * объект интерфейса для HTTP-запроса через Retrofit и
 * объект Retrofit.
 */

public class TranslatorApp extends Application {

    private final String TAG = "TranslatorApp";

    private final String BASE_URL = "https://translate.yandex.net/";

    private static TranslationApi sLocationApi;
    private Retrofit mRetrofit;


    @Override
    public void onCreate() {
        super.onCreate();
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        mRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
        sLocationApi = mRetrofit.create(TranslationApi.class);
    }

    public static TranslationApi getApi() {
        return sLocationApi;
    }

}
