package com.snlu.snluapp.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.snlu.snluapp.R;
import com.snlu.snluapp.data.ExtraValue;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Hong Tae Joon on 2016-11-15.
 */

public class SNLUAlertDialog extends DialogFragment {
    @BindView(R.id.dialog_alert_title) TextView textTitle;
    @BindView(R.id.dialog_alert_content) TextView textMessage;
    @BindView(R.id.dialog_alert_yes) View buttonYes;
    @BindView(R.id.dialog_alert_no) View buttonNo;
    private Object item;

    Dialog.OnClickListener onYesClickListener, onNoClickListener;
    View.OnClickListener defaultClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SNLUAlertDialog.this.dismiss();
        }
    };

    public static SNLUAlertDialog newInstance(String title, String mesasge, DialogInterface.OnClickListener onYesClickListener, DialogInterface.OnClickListener onNoClickListener) {
        SNLUAlertDialog dialog = new SNLUAlertDialog();
        Bundle args = new Bundle();
        args.putString(ExtraValue.TITLE, title);
        args.putString(ExtraValue.MESSAGE, mesasge);
        dialog.setArguments(args);
        dialog.setOnYesClickListener(onYesClickListener);
        dialog.setOnNoClickListener(onNoClickListener);
        return dialog;
    }

    public void setOnYesClickListener(Dialog.OnClickListener onYesClickListener) {
        this.onYesClickListener = onYesClickListener;
    }

    public void setOnNoClickListener(Dialog.OnClickListener onNoClickListener) {
        this.onNoClickListener = onNoClickListener;
    }

    public Object getItem() {
        return item;
    }

    public void setItem(Object item) {
        this.item = item;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.dialog_alert, null);
        ButterKnife.bind(this, v);

        textTitle.setText(getArguments().getString(ExtraValue.TITLE));
        textMessage.setText(getArguments().getString(ExtraValue.MESSAGE));

        if(onYesClickListener!=null) buttonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onYesClickListener.onClick(getDialog(), 0);
            }
        });
        else buttonYes.setOnClickListener(defaultClickListener);
        if(onNoClickListener!=null) buttonNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNoClickListener.onClick(getDialog(), 0);
            }
        });
        else buttonNo.setOnClickListener(defaultClickListener);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(v)
                .create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return dialog;
    }
}
