package com.ipg.workshop;

import android.bluetooth.BluetoothDevice;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.ipg.workshop.callbacks.OnUpdateReceiverListener;
import com.ipg.workshop.databinding.ActivityMainBinding;
import com.ipg.workshop.receivers.UpdateReceiver;

import info.plux.pluxapi.Communication;
import info.plux.pluxapi.Constants;
import info.plux.pluxapi.bitalino.BITalinoCommunication;
import info.plux.pluxapi.bitalino.BITalinoCommunicationFactory;
import info.plux.pluxapi.bitalino.BITalinoException;
import info.plux.pluxapi.bitalino.BITalinoFrame;
import info.plux.pluxapi.bitalino.bth.OnBITalinoDataAvailable;

import static info.plux.pluxapi.Constants.ACTION_STATE_CHANGED;
import static info.plux.pluxapi.Constants.ACTION_COMMAND_REPLY;
import static info.plux.pluxapi.Constants.ACTION_DATA_AVAILABLE;
import static info.plux.pluxapi.Constants.ACTION_DEVICE_READY;
import static info.plux.pluxapi.Constants.ACTION_EVENT_AVAILABLE;

public class MainActivity extends AppCompatActivity implements OnUpdateReceiverListener, View.OnClickListener, OnBITalinoDataAvailable {
    private ActivityMainBinding binding;

    public static final String EXTRA_DEVICE = "extra_device";
    public static final String FRAME = "frame";

    private BluetoothDevice device;
    private BITalinoCommunication biTalinoCommunication;
    private boolean isBitalino2 = false;

    private Handler handler;
    private Constants.States currentState = Constants.States.DISCONNECTED;

    private UpdateReceiver updateReceiver;
    private boolean isUpdateReceiverRegistered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        updateReceiver = new UpdateReceiver(this);

        if (getIntent() != null && getIntent().hasExtra(EXTRA_DEVICE))
            device = getIntent().getParcelableExtra(EXTRA_DEVICE);

        setUIElements();

        binding.startButton.setEnabled(false);
        binding.stopButton.setEnabled(false);
        binding.disconnectButton.setEnabled(false);

        handler = new Handler(getMainLooper()){

            @Override
            public void handleMessage(Message msg) {
                Bundle bundle = msg.getData();
                BITalinoFrame frame = bundle.getParcelable(FRAME);

                if (frame != null){
                    int value = frame.getAnalog(0);
                    binding.resultsTextView.setText(String.valueOf(value));

                    if (value > 100)
                        binding.resultsTextView.setTextColor(Color.RED);
                    else
                        binding.resultsTextView.setTextColor(Color.GREEN);
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(updateReceiver, makeUpdateIntentFilter());
        isUpdateReceiverRegistered = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (isUpdateReceiverRegistered){
            unregisterReceiver(updateReceiver);
            isUpdateReceiverRegistered = false;
        }

        if (biTalinoCommunication != null) {
            biTalinoCommunication.closeReceivers();

            try {
                biTalinoCommunication.disconnect();
            } catch (BITalinoException e) {
                e.printStackTrace();
            }
        }
    }

    private IntentFilter makeUpdateIntentFilter(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_STATE_CHANGED);
        intentFilter.addAction(ACTION_DATA_AVAILABLE);
        intentFilter.addAction(ACTION_EVENT_AVAILABLE);
        intentFilter.addAction(ACTION_DEVICE_READY);
        intentFilter.addAction(ACTION_COMMAND_REPLY);

        return intentFilter;
    }

    private void setUIElements(){
        if (device == null) return;

        binding.setDevice(device);
        binding.stateTextView.setText(currentState.name());

        Communication communication = Communication.getById(device.getType());
        if (communication == Communication.DUAL)
            communication = Communication.BLE;

        biTalinoCommunication = new BITalinoCommunicationFactory().getCommunication(communication, this, this);

        binding.connectButton.setOnClickListener(this);
        binding.disconnectButton.setOnClickListener(this);
        binding.startButton.setOnClickListener(this);
        binding.stopButton.setOnClickListener(this);
        binding.stateButton.setOnClickListener(this);
    }

    @Override
    public void onUpdateReceiver(Constants.States states) {
        binding.stateTextView.setText(states.name());

        switch (states){
            case NO_CONNECTION:
                binding.startButton.setEnabled(false);
                binding.stopButton.setEnabled(false);
                binding.disconnectButton.setEnabled(false);
                break;
            case CONNECTING:
                binding.layoutLoadingId.getRoot().setVisibility(View.VISIBLE);
                break;
            case CONNECTED:
                binding.startButton.setEnabled(true);
                binding.stopButton.setEnabled(false);
                binding.connectButton.setEnabled(false);
                binding.disconnectButton.setEnabled(true);
                binding.layoutLoadingId.getRoot().setVisibility(View.GONE);
                break;
            case ACQUISITION_OK:
                binding.stopButton.setEnabled(true);
                binding.startButton.setEnabled(false);
                break;
            case DISCONNECTED:
                binding.startButton.setEnabled(false);
                binding.stopButton.setEnabled(false);
                binding.disconnectButton.setEnabled(false);
                binding.connectButton.setEnabled(true);
                break;
        }
    }

    @Override
    public void onBITalinoDataAvailable(BITalinoFrame biTalinoFrame) {
        Message message = handler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putParcelable(FRAME, biTalinoFrame);
        message.setData(bundle);
        handler.sendMessage(message);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.connect_button:
                try {
                    biTalinoCommunication.connect(device.getAddress());
                } catch (BITalinoException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.disconnect_button:
                try {
                    biTalinoCommunication.disconnect();
                } catch (BITalinoException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.start_button:
                try {
                    biTalinoCommunication.start(new int[]{0, 1, 2, 3, 4, 5}, 1);
                } catch (BITalinoException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.stop_button:
                try {
                    biTalinoCommunication.stop();
                } catch (BITalinoException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.state_button:
                try {
                    biTalinoCommunication.state();
                } catch (BITalinoException e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}
