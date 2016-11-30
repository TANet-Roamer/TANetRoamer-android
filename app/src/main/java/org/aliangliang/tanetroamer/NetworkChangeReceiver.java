package org.aliangliang.tanetroamer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

        if(action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            NetworkInfo.State state = getNetworkState(intent);
            Log.i(Debug.TAG, "Receiver: Receive network change event: " + state);
            final ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
            if (activeNetwork != null && activeNetwork.isConnected()) {
                Log.d(Debug.TAG, "Receiver: State is connect");
                WifiManager manager = getWifiManager(context);
                String connectingSSID = getSSID(manager);
                if (connectingSSID.equals("Idontwanttosharewithyou") || connectingSSID.equals("TANetRoaming")) {
                    Log.d(Debug.TAG, "Receiver: Match TANetRoming");
                    Log.i(Debug.TAG, "Receiver: Start login service");
                    context.startService(new Intent(context, WifiLoginService.class));
                }
            } else {
                Log.d(Debug.TAG, "Receiver: Not connect yet");
            }
        }
    }

    private WifiManager getWifiManager(Context context) {
        return (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
    }

    private NetworkInfo.State getNetworkState(Intent intent) {
        NetworkInfo  networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        return networkInfo.getState();
    }

    private String getSSID(WifiManager manager) {
        return manager.getConnectionInfo().getSSID().replace("\"", "");
    }
}
