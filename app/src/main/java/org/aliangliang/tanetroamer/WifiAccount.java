package org.aliangliang.tanetroamer;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

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
import java.util.ArrayList;

class WifiAccount {

    public WifiAccount(Context context) throws JSONException {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        id_type = preferences.getString(ID_TYPE, null);
        school_studing = preferences.getString(KEY_SCHOOL, null);
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

        id_types = context.getResources().getStringArray(R.array.list_preference_entry_values);
        for (int i = 0; i < id_types.length; i++) {
            Log.d(Debug.TAG, "WifiAccount: id_types: " + id_types[i]);
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

    public String[] getUsernames() {
        ArrayList<String> username_sequence = new ArrayList<String>();
        username_sequence.add(preferences.getString("wifi_" + id_type + "_username", null));
        for (int i = 0; i < id_types.length; i++) {
            if(!id_types[i].equals(id_type)) {
                username_sequence.add(preferences.getString("wifi_" + id_types[i] + "_username", null));
            }
        }
        String[] usernames = username_sequence.toArray(new String[0]);
        for (int i = 0; i < usernames.length; i++) {
            Log.d(Debug.TAG, "WifiAccount: usernames: " + usernames[i]);
        }
        return _isLogin ? usernames : null;
    }

    public String[] getPasswords() {
        ArrayList<String> password_sequence = new ArrayList<String>();
        password_sequence.add(preferences.getString("wifi_" + id_type + "_password", null));
        for (int i = 0; i < id_types.length; i++) {
            if(!id_types[i].equals(id_type)) {
                password_sequence.add(preferences.getString("wifi_" + id_types[i] + "_password", null));
            }
        }
        String[] passwords = password_sequence.toArray(new String[0]);
        for (int i = 0; i < passwords.length; i++) {
            Log.d(Debug.TAG, "WifiAccount: passwords: " + passwords[i]);
        }
        return _isLogin ? passwords : null;
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
    private String[] id_types;
    private String username;
    private String password;
    private boolean _isLogin;
}