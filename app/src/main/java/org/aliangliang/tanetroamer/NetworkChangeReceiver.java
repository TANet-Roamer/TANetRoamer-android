package org.aliangliang.tanetroamer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.net.ConnectivityManager.TYPE_WIFI;

public class NetworkChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(Debug.TAG, "Receiver: Action:" + action);

        Log.d(Debug.TAG, "Receiver: Receive network event");

        ConnectivityManager connectManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectManager.getActiveNetworkInfo();

        if(networkInfo == null) {
            Log.d(Debug.TAG, "Receiver: No active network");
            return;
        }

        NetworkInfo.State state = networkInfo.getState();
        Log.i(Debug.TAG, "Receiver: Receive network change event: " + state);

        if(networkInfo.getType() != TYPE_WIFI) {
            Log.i(Debug.TAG, "Receiver: Not connect to wifi");
            return;
        }

        if(!networkInfo.isConnected()) {
            Log.d(Debug.TAG, "Receiver: Not finish connecting");
            return;
        }
        // Network conntected!!!

        context.startService(new Intent(context, WifiLoginService.class));
    }

    private WifiManager getWifiManager(Context context) {
        return (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
    }

    private String getSSID(WifiManager manager) {
        return manager.getConnectionInfo().getSSID().replace("\"", "");
    }
}
