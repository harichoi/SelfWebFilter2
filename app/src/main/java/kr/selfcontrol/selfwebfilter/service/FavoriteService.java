package kr.selfcontrol.selfwebfilter.service;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import kr.selfcontrol.selfwebfilter.dao.WebFilterDao;

/**
 * Created by hari on 17. 3. 3..
 */
public class FavoriteService {
    private static FavoriteService instance;
    WebFilterDao webFilterDao;
    List<String> favoriteUrls = new ArrayList<>();

    private FavoriteService(Context context) {

        favoriteUrls.add("http://google.com");
        favoriteUrls.add("http://cafe.daum.net");
        webFilterDao = new WebFilterDao(context);
    }

    public static FavoriteService getInstance(Context context) {
        if (instance == null) {
            instance = new FavoriteService(context);
        }

        return instance;
    }

    public String getFavoriteUrlsInHtml() {
        favoriteUrls = new ArrayList<>();
        List<String> favorites = webFilterDao.selectFavorites();

        for(String favorite : favorites) {
            favoriteUrls.add(favorite);
        }

        String result = "";
        for (String url : favoriteUrls) {
            result += "<a href=" + url + ">" + url + "</a><br>";
        }

        return result;
    }

    public void addFavorite(String url) {
        String full = url;
        String location = "";
        if (full.contains("://")) {
            location = full;
        } else {
            location = "http://" + full;
        }
        webFilterDao.insertFavorite(location);
    }

    public void removeFavorite(String url) {
        webFilterDao.removeFavorite(url);
    }

    public void removeFavoriteExactly(String url) {
        webFilterDao.removeFavoriteExactly(url);
    }
}
