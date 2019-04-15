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
import java.lang.Math;

public class DumbHandoffModule implements Runnable {

    private Context context;
    private WifiManager wifi;
    private List<ScanResult> results;
    private final int HANDOFF_PORT = 9003;
    private int trigger_threshold = 5;
    private final String PREFIX = "cs538";

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
            Log.d("WIFI STATUS", "ALREADY EXISTS");
            netId = getExistingNetworkId(wifiConf.SSID);
        }
        Log.d("NEW WIFI CODE", "" + netId);
        Log.d("OLD WIFI CODE", "" + old);
        wifi.disconnect();
        wifi.disableNetwork(old);
        wifi.enableNetwork(netId, true);
        wifi.reconnect();
        return true;
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

            String new_ssid = checkTrigger();
            if (new_ssid != "") {
                //send out updates

            }
        }
    };

    private String checkTrigger(){
        String curr_SSID = wifi.getConnectionInfo().getSSID();
        int old_signal = wifi.getConnectionInfo().getRssi();

        String best_SSID = curr_SSID;
        int max_strength = -100000;
        for (ScanResult scanResult : results) {
            if (scanResult.SSID.toLowerCase().contains(PREFIX)) {
                Log.d("WIFI INFO", scanResult.SSID + ":" + scanResult.level);
                if (scanResult.level > max_strength) {
                    best_SSID = scanResult.SSID;
                }
            }
        }

        if (max_strength > old_signal && Math.abs(max_strength - old_signal) > trigger_threshold) {
            //trigger handoff
            Log.d("WIFI INFO", "Triggering Handoff");
            connectToWifi(best_SSID);
            return best_SSID;
        }
        return "";
    }

}
