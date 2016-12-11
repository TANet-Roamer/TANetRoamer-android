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

/**
 * TANet Roaming 帳戶物件
 */
class WifiAccount {
    /**
     * @param context
     * @param id_type 指定此物件的帳號類型
     * @throws JSONException
     */
    public WifiAccount(Context context, String id_type) throws JSONException {
        this.id_type = id_type;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        school_studing = preferences.getString(KEY_SCHOOL, null);
        username = preferences.getString("wifi_" + id_type + "_username", null);
        password = preferences.getString("wifi_" + id_type + "_password", null);
        if (id_type.equals("itw"))
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
            Log.e(Debug.TAG, "WifiAccount: ", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                Log.e(Debug.TAG, "WifiAccount: ", e);
            }
        }

        String jsonString = writer.toString();
        JSONArray json;
        try {
            json = new JSONArray(jsonString);
        } catch (JSONException e) {
            Log.e(Debug.TAG, "WifiAccount: ", e);
            json = new JSONArray();
        }

        for (int i = 0; i < json.length(); i++) {
            JSONObject current_json = json.getJSONObject(i);
            if (current_json.getString("id").equals(school_studing)) {
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

    /**
     * 使用者是否有設定此類型的帳密
     *
     * @return Boolean 是否空資料
     */
    public Boolean isEmptyData() {
        return getUsername() == null || getPassword() == null;
    }

    private final static String KEY_SCHOOL = "school_studing";

    private SharedPreferences preferences;
    private String school_studing, username, password;
    private JSONObject school_data = new JSONObject();
    private String id_type;
}
