package kr.selfcontrol.selfwebfilter.service;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hari on 17. 3. 3..
 */
public class FavoriteService {
    private static FavoriteService instance;
    List<String> favoriteUrls = new ArrayList<>();

    private FavoriteService(Context context) {

        favoriteUrls.add("http://google.com");
        favoriteUrls.add("http://cafe.daum.net");
    }

    public static FavoriteService getInstance(Context context) {
        if (instance == null) {
            instance = new FavoriteService(context);
        }

        return instance;
    }

    public String getFavoriteUrlsInHtml() {
        String result = "";
        for (String url : favoriteUrls) {
            result += "<a href=" + url + ">" + url + "</a><br>";
        }

        return result;
    }

}
