package com.ipg.workshop.receivers;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ipg.workshop.adapters.AdapterDevices;

import java.util.Objects;

import info.plux.pluxapi.Constants;

public class ScanDevicesUpdateReceiver extends BroadcastReceiver {
    private AdapterDevices adapterDevices;

    public ScanDevicesUpdateReceiver(AdapterDevices adapterDevices){
        this.adapterDevices = adapterDevices;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && Objects.equals(intent.getAction(), Constants.ACTION_MESSAGE_SCAN)){
            BluetoothDevice device = intent.getParcelableExtra(Constants.EXTRA_DEVICE_SCAN);

            if (device != null)
                adapterDevices.addDevice(device);
        }
    }
}
