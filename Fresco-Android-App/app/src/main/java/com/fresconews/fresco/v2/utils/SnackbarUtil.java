package com.fresconews.fresco.v2.utils;

import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

import com.fresconews.fresco.R;

/**
 * Created by ryan on 6/21/2016.
 */
public class SnackbarUtil {

    public static void dismissableSnackbar(View view, CharSequence message) {
        dismissableSnackbar(view, message, Snackbar.LENGTH_INDEFINITE, null);
    }

    public static void dismissableSnackbar(View view, @StringRes int message) {
        dismissableSnackbar(view, message, Snackbar.LENGTH_INDEFINITE, null);
    }

    public static void dismissableSnackbar(View view, CharSequence message, View.OnClickListener listener) {
        dismissableSnackbar(view, message, Snackbar.LENGTH_INDEFINITE, listener);
    }

    public static void dismissableSnackbar(View view, @StringRes int message, View.OnClickListener listener) {
        dismissableSnackbar(view, message, Snackbar.LENGTH_INDEFINITE, listener);
    }

    public static void dismissableSnackbar(View view, CharSequence message, int duration) {
        dismissableSnackbar(view, message, duration, null);
    }

    public static void dismissableSnackbar(View view, @StringRes int message, int duration) {
        dismissableSnackbar(view, message, duration, null);
    }

    public static void dismissableSnackbar(View view, CharSequence message, int duration, View.OnClickListener listener) {
        if (view == null) {
            return;
        }
        Snackbar snackbar = Snackbar.make(view, message, duration);
        if (listener == null) {
            listener = view1 -> snackbar.dismiss();
        }
        snackbar.setAction(view.getContext().getString(R.string.dismiss), listener);
        TextView textView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        textView.setMaxLines(5);
        snackbar.show();
    }

    public static void dismissableSnackbar(View view, @StringRes int message, int duration, View.OnClickListener listener) {
        if (view == null) {
            return;
        }
        Snackbar snackbar = Snackbar.make(view, message, duration);
        if (listener == null) {
            listener = view1 -> snackbar.dismiss();
        }
        snackbar.setAction(view.getContext().getString(R.string.dismiss), listener);
        TextView textView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        textView.setMaxLines(5);
        snackbar.show();
    }

    public static void retrySnackbar(View view, String message, View.OnClickListener listener) {
        if (view == null) {
            return;
        }
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        if (listener == null) {
            listener = view1 -> snackbar.dismiss();
        }
        snackbar.setAction(view.getContext().getString(R.string.retry), listener);
        TextView textView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        textView.setMaxLines(5);
        snackbar.show();
    }

    public static void retrySnackbar(View view, @StringRes int message, View.OnClickListener listener) {
        if (view == null) {
            return;
        }
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        if (listener == null) {
            listener = view1 -> snackbar.dismiss();
        }
        snackbar.setAction(view.getContext().getString(R.string.retry), listener);
        TextView textView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        textView.setMaxLines(5);
        snackbar.show();
    }
}
