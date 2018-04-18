package com.ipg.workshop.utils;

import android.app.AlertDialog;
import android.content.Context;

import com.ipg.workshop.callbacks.DialogListener;

public class AlertDialogUtils {

    public static void showInfoDialog(Context context, String title, String message, DialogListener listener){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(title);
        builder.setMessage(message);
        builder.setOnDismissListener(dialogInterface -> {
            if (listener != null)
                listener.onDismissDialog();
        });

        builder.show();
    }
}
