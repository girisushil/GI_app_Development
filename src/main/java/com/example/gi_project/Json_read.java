package com.example.gi_project;

import android.app.Activity;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Json_read {
    public static String loadJSONFromAsset(AssetManager assetManager, String fileName) {
        String json = null;
        try {
            InputStream inputStream = assetManager.open(fileName);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            inputStream.close();
            json = stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }
    public String JsonData(Activity a,String Filename){
        AssetManager assetManager = a.getAssets();
        Json_read JSONUtils = new Json_read();
        String json = JSONUtils.loadJSONFromAsset(assetManager, Filename);
        return json;
    }
}
