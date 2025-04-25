package com.example.music_app.Server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIRetrofitClient {
    private static String currentBaseUrl = null;
    private static Retrofit retrofit = null;

    public static Retrofit getClient(String baseUrl) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Base URL cannot be null or empty");
        }

        if (retrofit == null || !baseUrl.equals(currentBaseUrl)) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Tạo TrustManager bỏ qua kiểm tra chứng chỉ
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Cài đặt SSLContext để bỏ qua kiểm tra chứng chỉ
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            try {
                final SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

                builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
                builder.hostnameVerifier((hostname, session) -> true);
            } catch (Exception e) {
                e.printStackTrace();
            }

            OkHttpClient okHttpClient = builder
                    .addInterceptor(loggingInterceptor)
                    .addInterceptor(chain -> {
                        Request originalRequest = chain.request();
                        Request newRequest = originalRequest.newBuilder()
                                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                                .build();
                        return chain.proceed(newRequest);
                    })
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .protocols(Arrays.asList(Protocol.HTTP_2, Protocol.HTTP_1_1))
                    .build();

            Gson gson = new GsonBuilder().create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();

            currentBaseUrl = baseUrl;
        }
        return retrofit;
    }
}