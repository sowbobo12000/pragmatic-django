package com.fresconews.fresco.v2.gallery.gallerydetail;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * Created by wumau on 11/30/2016.
 */

public class BackKeyEditText extends EditText {

    private OnBackKeyClickListener listener;

    public BackKeyEditText(Context context) {
        super(context);
    }

    public BackKeyEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BackKeyEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public BackKeyEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            boolean result = super.onKeyPreIme(keyCode, event);
            if (listener != null) {
                listener.onBackKeyClick();
            }
            return result; // So it is not propagated.
        }
        return super.dispatchKeyEvent(event);
    }

    public void setOnBackKeyClickListener(OnBackKeyClickListener listener) {
        this.listener = listener;
    }

    interface OnBackKeyClickListener {
        void onBackKeyClick();
    }
}
