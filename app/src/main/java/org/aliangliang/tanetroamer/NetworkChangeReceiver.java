package org.aliangliang.tanetroamer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

class NetworkChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        WifiAccount account = new WifiAccount(context);
        WifiManager manager = getWifiManager(context);
        Log.d(Debug.TAG, "Receiver: Receive network event");

        if (!account.isLogin()) {
            Log.i(Debug.TAG, "Receiver: Not login");
            return;
        }

        if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            NetworkInfo.State state = getNetworkState(intent);
            Log.i(Debug.TAG, "Receiver: Receive network change event");
            if (state == NetworkInfo.State.CONNECTED) { // Network is connect
                Log.d(Debug.TAG, "Receiver: State is connect");
                String connectingSSID = getSSID(manager);
                if (connectingSSID.equals("Idontwanttosharewithyou")) {
                    Log.d(Debug.TAG, "Receiver: Match TANetRoming");
                    Log.d(Debug.TAG, "Receiver: Start login service");
                    context.startService(new Intent(context, WifiLoginService.class));
                }
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