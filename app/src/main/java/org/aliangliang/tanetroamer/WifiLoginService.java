package org.aliangliang.tanetroamer;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

class WifiLoginService extends IntentService {
    public WifiLoginService() {
        super("WifiLoginService");
    }

    protected void onHandleIntent(Intent intent) {
        WifiAccount account = new WifiAccount(this);
        if (!account.isLogin()) {
            Log.i(Debug.TAG, "Service: Not login");
            return;
        }
        LoginWifi login = new LoginWifi(this, account.getUsername(), account.getPassword());
        Log.i(Debug.TAG, "Service: Start login");
        login.login();
    }
}