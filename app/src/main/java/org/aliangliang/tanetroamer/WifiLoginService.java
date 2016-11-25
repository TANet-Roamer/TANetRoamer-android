package org.aliangliang.tanetroamer;

import android.app.IntentService;
import android.content.Intent;
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

    }
}
