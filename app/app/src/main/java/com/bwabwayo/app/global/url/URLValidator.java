package com.bwabwayo.app.global.url;

import java.net.MalformedURLException;
import java.net.URL;

public class URLValidator {
    public static boolean isValidURL(String urlStr) {
        try {
            new URL(urlStr);
        } catch (MalformedURLException e) {
            return false;
        }
        return true;
    }
}