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
    public WifiAccount(Context context) throws JSONException {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        school_studing = preferences.getString(KEY_SCHOOL, null);
        id_type = preferences.getString(ID_TYPE, null);
        username = preferences.getString("wifi_" + id_type + "_username", null);
        password = preferences.getString("wifi_" + id_type + "_password", null);
        Boolean is_auto_login = preferences.getBoolean(IS_AUTO_LOGIN, true);
        _isLogin = username != null && password != null && is_auto_login;

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

    public void setLoginInfo(String username, String password) {
        SharedPreferences.Editor prefEditor = preferences.edit();
        _isLogin = true;
        this.username = username;
        this.password = password;
        prefEditor.putString("wifi_" + id_type + "_username", username);
        prefEditor.putString("wifi_" + id_type + "_password", password);
        prefEditor.apply();
    }

    public void clearLogin() {
        SharedPreferences.Editor prefEditor = preferences.edit();
        _isLogin = false;
        prefEditor.putString("wifi_" + id_type + "_username", null);
        prefEditor.putString("wifi_" + id_type + "_password", null);
        prefEditor.apply();
    }

    public JSONObject getSchoolData() {
        return school_data;
    }

    public String getUsername() {
        return _isLogin ? username : null;
    }

    public String getPassword() {
        return _isLogin ? password : null;
    }

    public boolean isLogin() {
        return _isLogin;
    }

    private final static String KEY_SCHOOL = "school_studing";
    private final static String ID_TYPE = "id_type";
    private final static String IS_AUTO_LOGIN = "is_auto_login";

    private SharedPreferences preferences;
    private String school_studing;
    private JSONObject school_data = new JSONObject();
    private String id_type;
    private String username;
    private String password;
    private boolean _isLogin;
}