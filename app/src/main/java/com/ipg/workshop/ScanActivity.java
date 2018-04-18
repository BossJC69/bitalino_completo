package com.ipg.workshop;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.ipg.workshop.adapters.AdapterDevices;
import com.ipg.workshop.databinding.ActivityScanBinding;
import com.ipg.workshop.receivers.ScanDevicesUpdateReceiver;
import com.ipg.workshop.utils.AlertDialogUtils;

import java.util.ArrayList;

import info.plux.pluxapi.BTHDeviceScan;
import info.plux.pluxapi.Constants;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

public class ScanActivity extends AppCompatActivity implements AdapterDevices.OnDevicesClickListener {
    private ActivityScanBinding binding;

    private AdapterDevices adapterDevices;
    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<BluetoothDevice> devices = new ArrayList<>();

    private boolean isScanning = false;
    private Handler handler;

    private BTHDeviceScan bthDeviceScan;
    private boolean isScanDevicesUpdateReceiverRegistered = false;

    private int REQUEST_ENABLE_BT = 10;
    private int PERMISSION_REQUEST_COARSE_LOCATION = 20;

    // Stop scanning after 10 seconds
    private long SCAN_PERIOD = 10000;

    private ScanDevicesUpdateReceiver scanDevicesUpdateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_scan);

        handler = new Handler();

        adapterDevices = new AdapterDevices(devices, this);

        scanDevicesUpdateReceiver = new ScanDevicesUpdateReceiver(adapterDevices);

        BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        assert manager != null;
        bluetoothAdapter = manager.getAdapter();

        if (bluetoothAdapter == null){
            // Bluetooth not supported
            Toast.makeText(this, "Error - Bluetooth not supported", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bthDeviceScan = new BTHDeviceScan(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.scan, menu);

        if (menu != null){
            if (!isScanning){
                menu.findItem(R.id.menu_stop).setVisible(false);
                menu.findItem(R.id.menu_scan).setVisible(true);
                menu.findItem(R.id.menu_refresh).setActionView(null);
            } else {
                menu.findItem(R.id.menu_stop).setVisible(true);
                menu.findItem(R.id.menu_scan).setVisible(false);
                menu.findItem(R.id.menu_refresh).setActionView(R.layout.loading_progress_bar);
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_scan:
                adapterDevices.clear();
                scanDevice(true);
                break;
            case R.id.menu_stop:
                scanDevice(false);
                break;
        }

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!hasPermissions()){
                permissionCheck();
                return;
            }
        }

        registerReceiver(scanDevicesUpdateReceiver, new IntentFilter(Constants.ACTION_MESSAGE_SCAN));
        isScanDevicesUpdateReceiverRegistered = true;

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!bluetoothAdapter.isEnabled()){
            Intent bthIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(bthIntent, REQUEST_ENABLE_BT);
        }

        binding.scanRv.setLayoutManager(new LinearLayoutManager(this));
        binding.scanRv.setAdapter(adapterDevices);

        scanDevice(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not enable BT
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED){
            finish();
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_COARSE_LOCATION){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                System.out.println("Permission granted");
                onResume();
            } else {
                AlertDialogUtils.showInfoDialog(this, getString(R.string.permission_denied_dialog_title),
                        getString(R.string.permission_denied_dialog_message), null);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        scanDevice(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (bthDeviceScan != null)
            bthDeviceScan.closeScanReceiver();

        if (isScanDevicesUpdateReceiverRegistered)
            unregisterReceiver(scanDevicesUpdateReceiver);
    }

    private void scanDevice(boolean enable){
        if (enable){
            // Stop scanning after SCAN_PERIOD
            handler.postDelayed(() -> {
                isScanning = false;
                bthDeviceScan.stopScan();
                invalidateOptionsMenu();
            }, SCAN_PERIOD);

            isScanning = true;
            bthDeviceScan.doDiscovery();
        } else {
            isScanning = false;
            bthDeviceScan.stopScan();
        }

        invalidateOptionsMenu();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean hasPermissions(){
        return checkSelfPermission(ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void permissionCheck(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
        }
    }

    @Override
    public void onDeviceClick(BluetoothDevice device) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_DEVICE, device);

        if (isScanning){
            bthDeviceScan.stopScan();
            isScanning = false;
        }

        startActivity(intent);
    }
}
