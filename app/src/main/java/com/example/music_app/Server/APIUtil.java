package com.example.music_app.Server;

public class APIUtil {
    private static final String BASE_URL = "http://192.168.0.197:8080/api/";

    public static APIService getService() {
        return APIRetrofitClient.getClient(BASE_URL).create(APIService.class);
    }
}
