package com.example.foghandoff;
import android.net.wifi.WifiManager;
import android.net.wifi.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.util.Log;
import android.net.wifi.WifiConfiguration;

import java.util.List;

public class DumbHandoffModule implements Runnable {

    private Context context;
    private WifiManager wifi;
    private List<ScanResult> results;
    private final int HANDOFF_PORT = 9003;

    public DumbHandoffModule(Context c){
        this.context = c;
    }

    @Override
    public void run(){
        wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (!wifi.isWifiEnabled()){
            wifi.setWifiEnabled(true);
        }

        scanWifi();
    }

    private void scanWifi() {
        IntentFilter intent = new IntentFilter(wifi.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(receiver, intent);
        boolean success = wifi.startScan();
        if (!success) {
            Log.d("WIFI", "Failed to scan");
        }
        Log.d("WIFI", "STARTING TO SCAN WIFI");
    }

    private boolean connectToWifi(String ssid) {


        wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (!wifi.isWifiEnabled()){
            wifi.setWifiEnabled(true);
        }

        WifiConfiguration wifiConf = new WifiConfiguration();
        wifiConf.SSID = String.format("\"%s\"", ssid);
        wifiConf.preSharedKey = "\"" + "networking" + "\"";
        wifiConf.status = WifiConfiguration.Status.ENABLED;
        wifiConf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        wifiConf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        wifiConf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);

        Log.d("WIFI STATUS", wifiConf.SSID + wifiConf.preSharedKey);

        int netId = wifi.addNetwork(wifiConf);
        //getExistingNetworkId(wifiConf.SSID);
        int old = wifi.getConnectionInfo().getNetworkId();

        if (netId == -1) {
            Log.d("NOOOO", "WAYYYYY");
        }
        Log.d("WIFI STATUS", "" + netId);
        Log.d("OLD WIFI CODE", "" + old);
        wifi.disconnect();
        wifi.disableNetwork(old);
        wifi.enableNetwork(netId, true);
        wifi.reconnect();
        return true;
    }

    private void assignHighestPriority(WifiConfiguration config) {
        List<WifiConfiguration> configuredNetworks = wifi.getConfiguredNetworks();
        if (configuredNetworks != null) {
            for (WifiConfiguration existingConfig : configuredNetworks) {
                if (config.priority <= existingConfig.priority) {
                    config.priority = existingConfig.priority + 1;
                }
            }
        }
    }

    private int getExistingNetworkId(String SSID) {
        List<WifiConfiguration> configuredNetworks = wifi.getConfiguredNetworks();
        if (configuredNetworks != null) {
            for (WifiConfiguration existingConfig : configuredNetworks) {

                if (existingConfig.SSID.equals(SSID)) {
                    return existingConfig.networkId;
                }
            }
        }
        return -1;
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent){
            results = wifi.getScanResults();
            context.unregisterReceiver(this);
            Log.d("IN", "Class");
            for (ScanResult scanResult : results) {
                Log.d("WIFI INFO", scanResult.SSID + ":" + scanResult.level);

                if (scanResult.SSID.toLowerCase().contains("bae")) {
                    Log.d("WIFI INFO", "Matches" + scanResult.SSID);
                    Log.d("WIFI CAPS", scanResult.capabilities);
                    connectToWifi(scanResult.SSID);
                    break;
                }
            }


        }
    };

}
