package org.aliangliang.tanetroamer;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

import org.json.JSONException;

public class WifiLoginService extends IntentService {

    private Context context;

    public WifiLoginService() {
        super("WifiLoginService");
        this.context = this;
    }

    /**
     * Return id of result message
     *
     * @param result Login result
     * @return ID of result message
     */
    private int getNotifyText(String result) {
        switch (result) {
            case GlobalValue.LOGIN_SUCCESS:
                return R.string.wifi_login_success;
            case GlobalValue.LOGIN_FAIL_AUTH_FAIL:
                return R.string.wifi_login_wrong_pwd;
            case GlobalValue.LOGIN_FAIL_DUPLICATE_USER:
                return R.string.wifi_login_duplicate_user;
            case GlobalValue.ALREADY_ONLINE:
                return R.string.wifi_login_already_online;
            case GlobalValue.LOGIN_FAIL_UNKNOWN_REASON:
            default:
                return R.string.wifi_login_unknown_reason;
        }
    }

    protected void onHandleIntent(Intent intent){
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
        Callback callable = new Callback() {
            public String call(String loginResult) throws Exception {
                Log.d(Debug.TAG, "Service: Callback!!!!!!!!!!!!!!");
                NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(), 0);
                Resources resources = getResources();
                int msgId = getNotifyText(loginResult);
                long[] vibrate_effect = (loginResult.equals(GlobalValue.LOGIN_SUCCESS))? new long[]{1000, 100} : new long[]{1000, 300, 300, 300};
                int light_color = (loginResult.equals(GlobalValue.LOGIN_SUCCESS))? Color.GREEN : Color.RED;
                Notification n = new Notification
                        .Builder(context)
                        .setContentTitle(resources.getString(R.string.app_name))
                        .setContentText(resources.getString(msgId))
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setTicker("EFFECT")
                        .setVibrate(vibrate_effect)
                        .setLights(light_color, 1000, 1000)
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                        .setContentIntent(contentIntent)
                        .build();
                nm.notify("TANet_Roamer_Login", 1, n);
                return null;
            }
        };
        try {
            login.login(callable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}