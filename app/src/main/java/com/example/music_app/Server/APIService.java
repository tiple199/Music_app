package com.example.music_app.Server;

public class APIService {
    private static String base_url = "https://tiep.lovestoblog.com/Server/";

    public static Dataservice getService(){
        return APIRetrofitClient.getClient(base_url).create(Dataservice.class);
    }
}