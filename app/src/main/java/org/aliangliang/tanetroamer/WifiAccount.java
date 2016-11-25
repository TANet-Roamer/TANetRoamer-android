package org.aliangliang.tanetroamer;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by ALiangLiang on 2016/11/25.
 */

public class WifiAccount {
    private SharedPreferences preferences;
    private String user;
    private String pwd;
    private String PREF_NAME;
    private String KEY_USERNAME;
    private String KEY_PASSWORD;
    public Boolean isLogin;

    public WifiAccount(Context context) {
        PREF_NAME = "pref_data_sync";
        KEY_USERNAME = "wifi_username";
        KEY_PASSWORD = "wifi_password";
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        user = preferences.getString(KEY_USERNAME, "testuser");
        pwd = preferences.getString(KEY_PASSWORD, "testpwd");
        Log.i("AutoLogin", user + "");
        Log.i("AutoLogin", pwd + "");
         isLogin = user != null && pwd != null;
    }

    public void setLoginInfo(String username, String password) {
        Editor prefEditor = preferences.edit();
        isLogin = true;
        this.user = user;
        this.pwd = password;
        prefEditor.putString(KEY_USERNAME, user);
        prefEditor.putString(KEY_PASSWORD, pwd);
        prefEditor.apply();
    }

    public void clearLogin() {
        Editor prefEditor = preferences.edit();
        this.isLogin = false;
        prefEditor.putString(KEY_USERNAME, null);
        prefEditor.putString(KEY_PASSWORD, null);
        prefEditor.apply();
    }

    public String getUsername() {
        if (isLogin) return user;
        else return null;
    }

    public String getPassword() {
        if (isLogin) return pwd;
        else return null;
    }
}
