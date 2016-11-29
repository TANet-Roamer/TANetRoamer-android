package org.aliangliang.tanetroamer;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.json.JSONException;

public class WifiLoginService extends IntentService {
    public WifiLoginService() {
        super("WifiLoginService");
    }

    protected void onHandleIntent(Intent intent) {
        WifiAccount account = null;
        try {
            account = new WifiAccount(this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (!account.isLogin()) {
            Log.i(Debug.TAG, "Service: Not login");
            return;
        }
        LoginWifi login = null;
        try {
            login = new LoginWifi(this, account.getUsername(), account.getPassword(), account.getSchoolData());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i(Debug.TAG, "Service: Start login");
        login.login();
    }
}