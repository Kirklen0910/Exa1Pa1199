package com.uth.exa1pa1199.data.remote;

import com.uth.exa1pa1199.BuildConfig;
import com.uth.exa1pa1199.data.session.SessionManager;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {
    private static final long CONNECT_TIMEOUT_SEGUNDOS = 10;
    private static final long READ_TIMEOUT_SEGUNDOS = 20;
    private static final long WRITE_TIMEOUT_SEGUNDOS = 20;

    private static volatile Retrofit retrofit;
    private static volatile RegistroApi api;
    private static volatile AuthApi authApi;

    private ApiClient() { }

    private static Retrofit getRetrofit() {
        if (retrofit == null) {
            synchronized (ApiClient.class) {
                if (retrofit == null) {
                    retrofit = new Retrofit.Builder()
                            .baseUrl(BuildConfig.API_BASE_URL)
                            .client(crearOkHttpClient())
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                }
            }
        }
        return retrofit;
    }

    public static RegistroApi getApi() {
        if (api == null) {
            synchronized (ApiClient.class) {
                if (api == null) api = getRetrofit().create(RegistroApi.class);
            }
        }
        return api;
    }

    public static AuthApi getAuthApi() {
        if (authApi == null) {
            synchronized (ApiClient.class) {
                if (authApi == null) authApi = getRetrofit().create(AuthApi.class);
            }
        }
        return authApi;
    }

    private static OkHttpClient crearOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)
                .addInterceptor(new AuthInterceptor(SessionManager.get()));

        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            logging.redactHeader("Authorization");
            builder.addInterceptor(logging);
        }
        return builder.build();
    }
}
