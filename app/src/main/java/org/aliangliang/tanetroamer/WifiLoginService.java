package org.aliangliang.tanetroamer;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * Created by ALiangLiang on 2016/11/25.
 */

public class WifiLoginService extends IntentService {

    public WifiLoginService(){
        super("AuthService");
    }

    public WifiLoginService(String name) {
        super(name);
    }

    protected void onHandleIntent(Intent intent) {
        WifiAccount account = new WifiAccount(this);
        if (!account.isLogin) {
            Log.i("AutoLogin", "Service: Not login");
            return;
        }
        LoginWifi login = new LoginWifi(this, account.getUsername(), account.getPassword());
        Log.i("AutoLogin", "Service: Start login");
        login.login();
    }
}
