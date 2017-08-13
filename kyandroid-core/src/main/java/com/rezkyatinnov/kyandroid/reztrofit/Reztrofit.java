package com.rezkyatinnov.kyandroid.reztrofit;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rezkyatinnov.kyandroid.session.Session;
import com.rezkyatinnov.kyandroid.session.SessionHelper;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.realm.RealmResults;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by rezkyatinnov on 08/08/2017.
 */

public class Reztrofit<T> {
    private Class<T> interfaceClass;
    private Context context;

    private T endpoint;
    private String baseUrl;
    private Retrofit retrofit;
    private List<Interceptor> interceptors = new ArrayList<>();
    private Map<String, String> headers = new HashMap<>();
    private static final Reztrofit instance = new Reztrofit();

    public static Reztrofit getInstance() {
        return instance;
    }

    private Reztrofit() {
        headers = new HashMap<>();
        interceptors = new ArrayList<>();
    }

    public void init(Context context, String baseUrl, Class<T> interfaceClass) {
        this.context = context;
        this.baseUrl = baseUrl;
        this.interfaceClass = interfaceClass;

        final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient defaultHttpClient = new OkHttpClient.Builder()
                .readTimeout(2 * 60, TimeUnit.SECONDS)
                .connectTimeout(2 * 60, TimeUnit.SECONDS)
                .addInterceptor(interceptor)
                .addInterceptor(chain -> {
                    Request.Builder builder = chain.request().newBuilder();

                    RealmResults<Session> sessions = SessionHelper.getRestHeaderSession();

                    for(Session session:sessions){
                        builder.addHeader(session.getKey(), session.getValue());
                    }

                    for (Map.Entry<String, String> entry : headers.entrySet()) {
                        builder.addHeader(entry.getKey(), entry.getValue());
                    }
                    return chain.proceed(builder.build());
                })
                .build();

        Gson gson = new GsonBuilder()
                .setLenient()
                .serializeNulls()
                .excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC)
                .create();

        retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(defaultHttpClient)
                .baseUrl(baseUrl)
                .build();

        endpoint = retrofit.create(this.interfaceClass);
    }

    public T getEndpoint() {
        return endpoint;
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }

    public void addInterceptor(Interceptor interceptor) {
        interceptors.add(interceptor);
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public void clearInterceptors() {
        interceptors.clear();
    }

    public Context getContext() {
        return context;
    }
}