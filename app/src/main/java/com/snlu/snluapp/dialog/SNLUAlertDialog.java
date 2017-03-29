package com.snlu.snluapp.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.snlu.snluapp.R;
import com.snlu.snluapp.activity.RoomActivity;

/**
 * Created by Hong Tae Joon on 2016-11-15.
 */

public class SNLUAlertDialog extends Dialog {
    String title;
    String content;
    Object item;

    Dialog.OnClickListener onYesClickListener, onNoClickListener;
    View.OnClickListener defaultClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SNLUAlertDialog.this.dismiss();
        }
    };

    public SNLUAlertDialog(Context context) {
        super(context);
    }

    @Override
    public void setTitle(CharSequence title) {
        this.title = title.toString();
    }

    public void setMessage(String content) {
        this.content = content;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_alert);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(params);

        TextView textTitle = (TextView)findViewById(R.id.dialog_alert_title);
        textTitle.setText(title);
        TextView textContent = (TextView)findViewById(R.id.dialog_alert_content);
        textContent.setText(content);

        if(onYesClickListener!=null) findViewById(R.id.dialog_alert_yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onYesClickListener.onClick(SNLUAlertDialog.this, 0);
            }
        });
        else findViewById(R.id.dialog_alert_yes).setOnClickListener(defaultClickListener);
        if(onNoClickListener!=null) findViewById(R.id.dialog_alert_no).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNoClickListener.onClick(SNLUAlertDialog.this, 0);
            }
        });
        else findViewById(R.id.dialog_alert_no).setOnClickListener(defaultClickListener);
    }
}
