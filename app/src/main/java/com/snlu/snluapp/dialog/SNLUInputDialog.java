package com.snlu.snluapp.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.snlu.snluapp.R;
import com.snlu.snluapp.data.ExtraValue;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Hong Tae Joon on 2016-11-15.
 */

public class SNLUInputDialog extends DialogFragment {
    @BindView(R.id.text_title) TextView textTitle;
    @BindView(R.id.edit_text) EditText editText;
    @BindView(R.id.button_cancel) View buttonCancel;
    @BindView(R.id.button_confirm) View buttonConfirm;
    private OnConfirmListener onConfirmListener;


    public static SNLUInputDialog newInstance(String title, String mesasge, OnConfirmListener onConfirmListener) {
        SNLUInputDialog dialog = new SNLUInputDialog();
        Bundle args = new Bundle();
        args.putString(ExtraValue.TITLE, title);
        args.putString(ExtraValue.MESSAGE, mesasge);
        dialog.setArguments(args);
        dialog.setOnConfirmListener(onConfirmListener);
        return dialog;
    }

    public SNLUInputDialog setOnConfirmListener(OnConfirmListener onConfirmListener) {
        this.onConfirmListener = onConfirmListener;
        return this;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.dialog_input, null);
        ButterKnife.bind(this, v);

        textTitle.setText(getArguments().getString(ExtraValue.TITLE));
        editText.setText(getArguments().getString(ExtraValue.MESSAGE));

        if(onConfirmListener!=null) buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onConfirmListener.onConfirm(editText.getText().toString());
                dismiss();
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(v)
                .create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return dialog;
    }

    public interface OnConfirmListener {
        void onConfirm(String text);
    }
}
