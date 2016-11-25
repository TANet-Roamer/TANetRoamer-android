package org.aliangliang.tanetroamer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * Created by ALiangLiang on 2016/11/25.
 */

public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        WifiAccount account = new WifiAccount(context);
        WifiManager manager = getWifiManager(context);
        if (!account.isLogin) {
            return;
        }

        if (action == WifiManager.NETWORK_STATE_CHANGED_ACTION) {
            NetworkInfo.State state = getNetworkState(intent);
            if (state == NetworkInfo.State.CONNECTED) { // Network is connect
                String connectingSSID = (String)getSSID(manager);
                Log.i("AutoLogin", connectingSSID + "");
                if (connectingSSID == "Idontwanttosharewithyou") {
                    Log.i("AutoLogin", "成功連上目標 WIFI。");
                    context.startService(new Intent(context, WifiLoginService.class));
                }
            }
        }
    }

    private WifiManager getWifiManager(Context context) {
        return (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
    }

    private NetworkInfo.State getNetworkState(Intent intent) {
        NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        return networkInfo.getState();
    }

    private String getSSID(WifiManager manager) {
        return manager.getConnectionInfo().getSSID().replace("\"", "");
    }
}
