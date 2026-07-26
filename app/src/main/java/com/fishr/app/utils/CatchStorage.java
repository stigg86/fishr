package com.fishr.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.*;

public class CatchStorage {
    private static final String PREF_NAME = "fishr_catches";
    private static final String KEY_CATCHES = "catches";

    public static void save(Context context, CatchEntry entry) {
        List<CatchEntry> catches = loadAll(context);
        catches.add(0, entry);  // newest first
        
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        JSONArray array = new JSONArray();
        for (CatchEntry c : catches) {
            array.put(catchToJson(c));
        }
        prefs.edit().putString(KEY_CATCHES, array.toString()).apply();
    }

    public static List<CatchEntry> loadAll(Context context) {
        List<CatchEntry> catches = new ArrayList<>();
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_CATCHES, "[]");
        
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                catches.add(jsonToCatch(array.getJSONObject(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return catches;
    }

    public static void delete(Context context, String id) {
        List<CatchEntry> catches = loadAll(context);
        catches.removeIf(c -> c.id.equals(id));
        
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        JSONArray array = new JSONArray();
        for (CatchEntry c : catches) {
            array.put(catchToJson(c));
        }
        prefs.edit().putString(KEY_CATCHES, array.toString()).apply();
    }

    static JSONObject catchToJson(CatchEntry c) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("id", c.id);
            obj.put("species", c.species);
            obj.put("weight", c.weight);
            obj.put("length", c.length);
            obj.put("notes", c.notes);
            obj.put("latitude", c.latitude);
            obj.put("longitude", c.longitude);
            obj.put("timestamp", c.timestamp);
            obj.put("photoPath", c.photoPath);
        } catch (Exception e) {}
        return obj;
    }

    static CatchEntry jsonToCatch(JSONObject obj) {
        CatchEntry c = new CatchEntry();
        try {
            c.id = obj.optString("id", "");
            c.species = obj.optString("species", "Unknown");
            c.weight = obj.optDouble("weight", 0);
            c.length = obj.optDouble("length", 0);
            c.notes = obj.optString("notes", "");
            c.latitude = obj.optDouble("latitude", 0);
            c.longitude = obj.optDouble("longitude", 0);
            c.timestamp = obj.optString("timestamp", "");
            c.photoPath = obj.optString("photoPath", "");
        } catch (Exception e) {}
        return c;
    }
}
