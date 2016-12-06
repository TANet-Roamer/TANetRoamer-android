package org.aliangliang.tanetroamer;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;

public class WifiLoginService extends IntentService {

    private Context context;

    public WifiLoginService() {
        super("WifiLoginService");
        this.context = this;
    }

    private boolean checkSsid() {
        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        String connectingSSID = wifiManager.getConnectionInfo().getSSID().replace("\"", "");
        Log.d(Debug.TAG, "SSID: " + connectingSSID);
        return connectingSSID.equals(context.getResources().getString(R.string.wifi_login_SSID));
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
            case GlobalValue.ALREADY_ONLINE:
                return R.string.wifi_login_already_online;
            case GlobalValue.LOGIN_FAIL_ONLY_ONE_USER:
                return R.string.wifi_login_only_one_user;
            case GlobalValue.LOGIN_FAIL_NO_INFORMATION:
                return R.string.wifi_login_empty_user_pwd;
            case GlobalValue.LOGIN_FAIL_CONNECT_TIMEOUT:
                return R.string.wifi_login_connect_timeout;
            case GlobalValue.LOGIN_FAIL_UNKOWN_HOST:
                return R.string.wifi_login_host_can_not_found;
            case GlobalValue.LOGIN_FAIL_UNKNOWN_REASON:
            default:
                return R.string.wifi_login_unknown_reason;
        }
    }

    protected void onHandleIntent(Intent intent){

        Log.d(Debug.TAG, "Receiver: State is connect");
        if(!checkSsid()) return;
        Log.i(Debug.TAG, "Receiver: Start login service");

        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            Boolean is_auto_login = preferences.getBoolean(IS_AUTO_LOGIN, true);
            if(!is_auto_login) {
                Log.i(Debug.TAG, "Service: Coz user inactive auto-login function, stop login process.");
                return;
            }
            ArrayList<WifiAccount> accounts = new ArrayList<WifiAccount>();
            String id_type = preferences.getString(ID_TYPE, null);
            WifiAccount account = new WifiAccount(this, id_type);
            if(!account.isEmptyData())
                accounts.add(account);
            if(preferences.getBoolean("retry_with_another_account", false)) {
                String[] id_types = context.getResources().getStringArray(R.array.list_preference_entry_values);
//                String id_type = preferences.getString(ID_TYPE, null);
                for (String idType : id_types) {
                    if(!idType.equals(id_type)) {
                        account = new WifiAccount(this, idType);
                        if(!account.isEmptyData())
                            accounts.add(account);
                    }
                }
            }
            final LoginWifi login = new LoginWifi(this, accounts);
            Log.i(Debug.TAG, "Service: Start login");

            final Callback callable = new Callback() {
                public String call(String loginResult) throws Exception {
                    Log.d(Debug.TAG, "Service: Callback!!!!!!!!!!!!!!");
                    NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                    PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(), 0);
                    Resources resources = getResources();
                    Boolean isSuccess = loginResult.equals(GlobalValue.LOGIN_SUCCESS);
                    int msgId = getNotifyText(loginResult);
                    long[] vibrate_effect = (isSuccess)? new long[]{1000, 100} : new long[]{1000, 300};
                    int light_color = (isSuccess)? Color.GREEN : Color.RED;
                    String msg = resources.getString(msgId);
                    Notification.BigTextStyle style = new Notification.BigTextStyle().bigText(msg);
                    Notification.Builder nb = new Notification
                        .Builder(context)
                        .setStyle(style)
                        .setContentTitle(resources.getString(R.string.app_name))
                        .setContentText(msg)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
                        .setContentIntent(contentIntent);
                    if(!loginResult.equals(GlobalValue.ALREADY_ONLINE))
                        nb.setTicker("EFFECT")
                            .setVibrate(vibrate_effect)
                            .setLights(light_color, 1000, 1000);
                    Notification n = nb.build();
                    nm.notify("TANet_Roamer_Login", 1, n);
                    return null;
                }
            };
            login.login(callable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private final static String ID_TYPE = "id_type";
    private final static String IS_AUTO_LOGIN = "is_auto_login";
}
