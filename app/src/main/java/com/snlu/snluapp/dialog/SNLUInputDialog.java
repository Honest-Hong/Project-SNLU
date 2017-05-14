package com.snlu.snluapp.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.snlu.snluapp.R;

/**
 * Created by Hong Tae Joon on 2016-11-15.
 */

public class SNLUInputDialog extends Dialog {
    private String title;
    private String content;
    private EditText editText;
    private OnConfirmListener onConfirmListener;

    public SNLUInputDialog(Context context) {
        super(context);
    }

    public SNLUInputDialog setTitleText(CharSequence title) {
        this.title = title.toString();
        return this;
    }

    public SNLUInputDialog setOnConfirmListener(OnConfirmListener onConfirmListener) {
        this.onConfirmListener = onConfirmListener;
        return this;
    }

    public SNLUInputDialog setContent(String content) {
        this.content = content;
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_input);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(params);

        TextView textTitle = (TextView)findViewById(R.id.text_title);
        textTitle.setText(title);
        editText = (EditText)findViewById(R.id.edit_text);
        editText.setText(content);
        findViewById(R.id.button_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onConfirmListener.onConfirm(editText.getText().toString());
                dismiss();
            }
        });
        findViewById(R.id.button_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public interface OnConfirmListener {
        void onConfirm(String text);
    }
}
