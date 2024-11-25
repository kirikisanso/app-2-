package com.landtanin.teacherattendancemonitoring.manager;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by landtanin on 4/19/2017 AD.
 */

public class HttpManager {

    public static final String BASE_URL = "http://landtanin.bitnamiapp.com/api/";
    private static Retrofit retrofit = null;
    private static Gson gson;

    public static Retrofit getInstance(){

        if(retrofit==null){

            gson=new GsonBuilder().setPrettyPrinting().create();
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new LoggingInterceptor())
                    .connectTimeout(10, TimeUnit.MINUTES)
                    .build();

            Gson gson2 = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd HH:mm:ss")
                    .create();
//
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(BASE_URL)
////                .baseUrl("https://nuuneoi.com/courses/500px/")
//                .addConverterFactory(GsonConverterFactory.create(gson2))
//                .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson2))
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .client(client)
                    .build();

        }

        return retrofit;

    }

    private static class LoggingInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            // Log query Url
            long t1 = System.nanoTime();
            System.out.println(
                    String.format("intercept Sending request %s, method %s, body %s, tag %s on %s%n%s",
                            request.url(), request.method(), request.body(), request.tag(), chain.connection(),
                            request.headers()));


            // Log all JSON data
            Log.e("intercept: ", String.valueOf(request.url()));

            Response response = chain.proceed(request);

            long t2 = System.nanoTime();
            System.out.println(
                    String.format("intercept Received response for %s in %.1fms%n%s", response.request().url(),
                            (t2 - t1) / 1e6d, response.headers()));

            String bodyString = response.body().string();

            //Logข้อมูลทั้งหมด

            Log.e( "intercept bodyString: ", bodyString);
            try {
                JSONObject jsonObject = new JSONObject(bodyString);

////                Logข้อมูลJsonทั้งหมด
                //  Log.e(TAG,gson.toJson(jsonObject));

            } catch (JSONException e) {

                e.printStackTrace();
            }

            return response.newBuilder()
                    .body(ResponseBody.create(response.body().contentType(), bodyString))
                    .build();
        }
    }


}
