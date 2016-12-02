package org.aliangliang.tanetroamer;

import android.content.BroadcastReceiver;
import android.content.Context;
import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.WIFI_SERVICE;
import android.content.Intent;
import android.net.ConnectivityManager;
import static android.net.ConnectivityManager.TYPE_WIFI;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.json.JSONException;

public class NetworkChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(Debug.TAG, "Receiver: Action:" + action);

        WifiAccount account = null;
        try {
            account = new WifiAccount(context);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(Debug.TAG, "Receiver: Receive network event");

        if (!account.isLogin()) {
            Log.i(Debug.TAG, "Receiver: Not login");
            return;
        }

        ConnectivityManager connectManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectManager.getActiveNetworkInfo();
        if(networkInfo == null) {
            Log.d(Debug.TAG, "Receiver: No active network");
            return;
        }

        NetworkInfo.State state = networkInfo.getState();
        Log.i(Debug.TAG, "Receiver: Receive network change event: " + state);

        if(networkInfo != null && networkInfo.getType() != TYPE_WIFI) {
            Log.i(Debug.TAG, "Receiver: Not connect to wifi");
            return;
        }

        if(!networkInfo.isConnected()) {
            Log.d(Debug.TAG, "Receiver: Not finish connecting");
            return;
        }

        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);

        Log.d(Debug.TAG, "Receiver: State is connect");
        String connectingSSID = getSSID(wifiManager);
        Log.d(Debug.TAG, "SSID: " + connectingSSID);
        if (connectingSSID.equals("TANetRoaming")) {
            Log.d(Debug.TAG, "Receiver: Match TANetRoming");
            Log.i(Debug.TAG, "Receiver: Start login service");
            context.startService(new Intent(context, WifiLoginService.class));
        }
    }

    private WifiManager getWifiManager(Context context) {
        return (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
    }

    private String getSSID(WifiManager manager) {
        return manager.getConnectionInfo().getSSID().replace("\"", "");
    }
}
