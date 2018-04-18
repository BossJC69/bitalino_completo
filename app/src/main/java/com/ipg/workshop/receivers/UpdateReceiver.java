package com.ipg.workshop.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;

import com.ipg.workshop.callbacks.OnUpdateReceiverListener;

import java.util.Objects;

import info.plux.pluxapi.Constants;
import info.plux.pluxapi.bitalino.BITalinoFrame;

import static info.plux.pluxapi.Constants.ACTION_DATA_AVAILABLE;
import static info.plux.pluxapi.Constants.ACTION_STATE_CHANGED;
import static info.plux.pluxapi.Constants.EXTRA_DATA;
import static info.plux.pluxapi.Constants.EXTRA_STATE_CHANGED;
import static info.plux.pluxapi.Constants.IDENTIFIER;

public class UpdateReceiver extends BroadcastReceiver {
    private OnUpdateReceiverListener listener;

    public UpdateReceiver(OnUpdateReceiverListener listener){
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;

        if (Objects.equals(intent.getAction(), ACTION_STATE_CHANGED)){

            String identifier = intent.getStringExtra(IDENTIFIER);
            Constants.States states = Constants.States.getStates(intent.getIntExtra(EXTRA_STATE_CHANGED, 0));

            listener.onUpdateReceiver(states);

        } else if (ACTION_DATA_AVAILABLE.equals(intent.getAction())){
            if (intent.hasExtra(EXTRA_DATA)){
                Parcelable parcelable = intent.getParcelableExtra(EXTRA_DATA);

                if (parcelable.getClass() == BITalinoFrame.class) { //BITalino
                    BITalinoFrame frame = (BITalinoFrame) parcelable;
                }
            }
        }
    }
}
