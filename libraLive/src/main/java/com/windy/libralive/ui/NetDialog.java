package com.windy.libralive.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.windy.libralive.R;

public class NetDialog extends Dialog {

    public NetDialog(@NonNull Context context) {
        super(context);
    }

    public NetDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected NetDialog(@NonNull Context context, boolean cancelable, @Nullable DialogInterface.OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.net_work_dialog);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.GREEN));
        setCanceledOnTouchOutside(false);
    }
}
