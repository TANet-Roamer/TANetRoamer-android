package org.aliangliang.tanetroamer;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

class WifiAccount {

    public WifiAccount(Context context, String id_type) throws JSONException {
        this.id_type = id_type;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        school_studing = preferences.getString(KEY_SCHOOL, null);
        username = preferences.getString("wifi_" + id_type + "_username", null);
        password = preferences.getString("wifi_" + id_type + "_password", null);
        if(id_type.equals("itw"))
            username += "@itw";

        InputStream is = context.getResources().openRawResource(R.raw.units);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String jsonString = writer.toString();
        JSONArray json;
        try {
            json = new JSONArray(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
            json = new JSONArray();
        }

        for(int i = 0; i< json.length(); i++) {
            JSONObject current_json = json.getJSONObject(i);
            if(current_json.getString("id").equals(school_studing)) {
                school_data = current_json;
            }
        }
    }

    public JSONObject getSchoolData() {
        return school_data;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Boolean isEmptyData() {
        return getUsername() == null || getPassword() == null;
    }

    private final static String KEY_SCHOOL = "school_studing";

    private SharedPreferences preferences;
    private String school_studing, username, password;
    private JSONObject school_data = new JSONObject();
    private String id_type;
    private String[] id_types;
}
