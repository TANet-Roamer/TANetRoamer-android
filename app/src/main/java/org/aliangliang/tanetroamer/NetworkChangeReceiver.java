package org.aliangliang.tanetroamer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.net.ConnectivityManager.TYPE_WIFI;

public class NetworkChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(Debug.TAG, "Receiver: 事件:" + action);

        Log.d(Debug.TAG, "Receiver: 收到 network 事件");

        ConnectivityManager connectManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectManager.getActiveNetworkInfo();

        if (networkInfo == null) {
            Log.d(Debug.TAG, "Receiver: 沒有運作中的網路介面");
            return;
        }

        NetworkInfo.State state = networkInfo.getState();
        Log.i(Debug.TAG, "Receiver: 收到 network 改變的事件: " + state);

        // 檢查連線方式是否為 WIFI
        if (networkInfo.getType() != TYPE_WIFI) {
            Log.i(Debug.TAG, "Receiver: 未連上WIFI");
            return;
        }

        // 檢查是否連網
        if (!networkInfo.isConnected()) {
            Log.d(Debug.TAG, "Receiver: 還未連網");
            return;
        }

        // 確定已連上網路
        // 開始進行事件處理
        context.startService(new Intent(context, WifiLoginService.class));
    }
}
