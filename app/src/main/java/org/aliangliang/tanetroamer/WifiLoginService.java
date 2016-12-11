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

    /**
     * 回傳 WIFI 的SSID
     *
     * @return String SSID
     */
    private String getSsid() {
        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        String connectingSSID = wifiManager.getConnectionInfo().getSSID().replace("\"", "");
        Log.d(Debug.TAG, "SSID: " + connectingSSID);
        return connectingSSID;
    }

    /**
     * Return id of result message
     *
     * @param result 登入結果
     * @return 登入結果的訊息 ID
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

    /**
     * 處理收到連線 WIFI 網路後的事情
     *
     * @param intent
     */
    protected void onHandleIntent(Intent intent) {
        final String SSID = getSsid();
        // 透過檢查 SSID，確認是否為 TANet Roaming。
        if (!SSID.equals(context.getResources().getString(R.string.wifi_login_SSID))) {
            // 不是的話，將 SSID 儲存。
            lastLoginSSID = SSID;
            return;
        }
        Log.i(Debug.TAG, "Receiver: 啟動登入服務 (Service)");

        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            // 抓取設定「是否自動登入」
            Boolean is_auto_login = preferences.getBoolean(IS_AUTO_LOGIN, true);
            if (!is_auto_login) {
                Log.i(Debug.TAG, "Service: 因為使用者關閉「自動登入」，停止登入程序.");
                return;
            }
            // 建立帳號清單
            ArrayList<WifiAccount> accounts = new ArrayList<WifiAccount>();
            // 將使用者優先使用的帳戶，加進帳號清單。
            String id_type = preferences.getString(ID_TYPE, null);
            WifiAccount account = new WifiAccount(this, id_type);
            if (!account.isEmptyData()) // 若帳號資訊不為空，加進清單。
                accounts.add(account);
            // 如果使用者開啟「使用其他帳戶重試」
            if (preferences.getBoolean("retry_with_another_account", false)) {
                // 將其他種類帳戶加進清單後面。
                String[] id_types = context.getResources().getStringArray(R.array.list_preference_entry_values);
                for (String idType : id_types) {
                    // 因為使用者優先使用的帳號已加進清單，所以必須過濾排除。
                    if (!idType.equals(id_type)) {
                        account = new WifiAccount(this, idType);
                        if (!account.isEmptyData()) // 若帳號資訊不為空，加進清單。
                            accounts.add(account);
                    }
                }
            }
            // 建立「登入」物件
            final LoginWifi login = new LoginWifi(this, accounts);
            Log.i(Debug.TAG, "Service: 啟動登入程序");

            login.login((loginResult) -> { // 回呼函數
                Log.d(Debug.TAG, "Service: Callback!!!!!!!!!!!!!!");
                NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(), 0);
                Resources resources = getResources();
                Boolean isSuccess = loginResult.equals(GlobalValue.LOGIN_SUCCESS);
                // 如果已經連上網際網路
                if (loginResult.equals(GlobalValue.ALREADY_ONLINE)) {
                    // 如果這次連線的 SSID 與上次相同
                    if (lastLoginSSID.equals(SSID)) {
                        Log.i(Debug.TAG, "Service Callback: 已經登入，且與上次連線是同一個 WIFI SSID。");
                        return null;
                    }
                    // 儲存這次的 SSID
                    lastLoginSSID = SSID;
                    Log.i(Debug.TAG, "Service Callback: 已經登入，但與上次連線是不同 WIFI SSID。");
                }
                // 開始建立「通知」
                // 取得登入結果的訊息 ID
                int msgId = getNotifyText(loginResult);
                // 設定震動模式
                long[] vibrate_effect = (isSuccess) ? new long[]{1000, 100} : new long[]{1000, 300};
                // 設定呼吸燈顏色
                int light_color = (isSuccess) ? Color.GREEN : Color.RED;
                // 如果登入成功，則儲存此次的 SSID
                lastLoginSSID = (isSuccess) ? SSID : lastLoginSSID;
                // 取得告知使用者的登入結果訊息文字
                String msg = resources.getString(msgId);
                // 設定「通知」樣式
                Notification.BigTextStyle style = new Notification.BigTextStyle().bigText(msg);
                // 建立「Notification.Builder」
                Notification.Builder nb = new Notification
                    .Builder(context)
                    .setStyle(style)
                    .setContentTitle(resources.getString(R.string.app_name))
                    .setContentText(msg)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
                    .setContentIntent(contentIntent);
                // 如果已經連上網際網路，不使用震動、呼吸燈。
                if (!loginResult.equals(GlobalValue.ALREADY_ONLINE))
                    nb.setTicker("EFFECT")
                        .setVibrate(vibrate_effect)
                        .setLights(light_color, 1000, 1000);
                Notification n = nb.build();
                // 通知
                nm.notify("TANet_Roamer_Login", 1, n);
                return null;

            });
        } catch (Exception e) {
            Log.e(Debug.TAG, "WifiLoginService: ", e);
        }
    }

    private static String lastLoginSSID = "";
    private final static String ID_TYPE = "id_type";
    private final static String IS_AUTO_LOGIN = "is_auto_login";
}
