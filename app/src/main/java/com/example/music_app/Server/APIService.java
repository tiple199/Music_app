package com.example.music_app.Server;

/**
 * Service class to provide API client for data operations.
 */
public class APIService {
    private static final String BASE_URL = "http://tiep.lovestoblog.com/Server/";

    public static Dataservice getService() {
        return APIRetrofitClient.getClient(BASE_URL).create(Dataservice.class);
    }
}