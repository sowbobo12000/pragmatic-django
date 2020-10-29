package com.fresconews.fresco.framework.databinding.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.databinding.BindingAdapter;
import android.graphics.Point;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.databinding.bindingTypes.BindableString;
import com.fresconews.fresco.framework.databinding.bindingTypes.BindableView;

import rx.functions.Action1;

/**
 * Created by Ryan on 6/9/2016.
 */
public class ViewAdapters {
    @BindingAdapter({"onCreate"})
    public static <T extends View> void onViewCreated(View view, Action1<T> action) {
        //noinspection unchecked
        action.call((T) view);
    }

    @BindingAdapter({"onClick"})
    public static <T extends View> void onViewClicked(View view, Action1<T> action) {
        view.setOnClickListener(v -> {
            //noinspection unchecked
            action.call((T) view);
        });
    }

    @BindingAdapter({"onLongClick"})
    public static <T extends View> void onViewLongClicked(View view, Action1<T> action) {
        view.setOnLongClickListener(v -> {
            //noinspection unchecked
            action.call((T) view);
            return true;
        });
    }

//    @BindingAdapter({"height"})
//    public static<T extends View> void onViewHeightBound(FrameLayout frameLayout, float height){
//
//        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(DimensionUtils.convertPixelsToDp(frameLayout.getWidth()), height);
//        frameLayout.setLayoutParams(lp);
//    }

    @BindingAdapter({"height"})
    public static <T extends View> void onViewHeightBound(FrameLayout frameLayout, int height) {
        WindowManager wm = (WindowManager) Fresco2.getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, height);
        frameLayout.setLayoutParams(lp);
        frameLayout.requestLayout();
    }

    @BindingAdapter({"height"})
    public static <T extends View> void onViewHeightBound(CollapsingToolbarLayout toolbarLayout, int height) {
        WindowManager wm = (WindowManager) Fresco2.getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(width, height);
        toolbarLayout.setLayoutParams(lp);
        toolbarLayout.requestLayout();
    }

    @BindingAdapter({"bind"})
    public static <T extends View> void bindView(View view, BindableView<T> bindableView) {
        if (bindableView == null) {
            return;
        }
        //noinspection unchecked
        bindableView.set((T) view);
    }

    @BindingAdapter({"bindValue"})
    public static void bindEditText(EditText view, BindableString bindableString) {
        if (bindableString == null) {
            return;
        }

        if (view.getTag(R.id.bound) == null) {
            view.setTag(R.id.bound, true);
            view.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    bindableString.set(s.toString());
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
        }
        String newText = bindableString.get();
        if (!view.getText().toString().equals(newText)) {
            view.setText(newText);
        }
    }

    @BindingAdapter({"onProgressChange"})
    public static void bindProgressChanged(SeekBar seekBar, Action1<Integer> action) {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    action.call(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @BindingAdapter({"fabBackground"})
    public static void setFabBackground(FloatingActionButton fab, int color){
//        fab.setBackgroundColor(color); //I/FloatingActionButton: Setting a custom background is not supported. // well, fuck you too, why even offer it?
        try {
            fab.setBackgroundTintList(new ColorStateList(new int[][]{new int[]{0}}, new int[]{color}));
        } catch(Exception e){
            //pretty sure i don't have to do anything here.
        }
    }


}
