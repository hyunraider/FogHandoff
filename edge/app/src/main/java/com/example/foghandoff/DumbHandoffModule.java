package com.example.foghandoff;
import android.net.wifi.WifiManager;
import android.net.wifi.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.util.Log;

import java.util.List;

public class DumbHandoffModule implements Runnable {

    private Context context;
    private WifiManager wifi;
    private List<ScanResult> results;

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

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent){
            results = wifi.getScanResults();
            context.unregisterReceiver(this);
            Log.d("IN", "Class");
            Log.d("DEBUG", results.toString());
            for (ScanResult scanResult : results) {
                Log.d("WIFI INFO", scanResult.SSID + ":" + scanResult.level);
            }
        }
    };

}
