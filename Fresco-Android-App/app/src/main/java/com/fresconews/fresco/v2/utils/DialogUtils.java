package com.fresconews.fresco.v2.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import com.fresconews.fresco.R;

/**
 * Created by wumau on 12/13/2016.
 */
public class DialogUtils {

    public static void showFrescoDialog(Context context, @LayoutRes int layout, DialogInterface.OnClickListener positiveClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setView(layout)
                .setPositiveButton(R.string.ok, positiveClickListener)
                .setCancelable(true);
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.fresco_blue));
        });
        dialog.show();
    }

    public static void showFrescoDialog(Context context, @LayoutRes int layout, DialogInterface.OnClickListener positiveClickListener, DialogInterface.OnClickListener negativeClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setView(layout)
                .setPositiveButton(R.string.ok, positiveClickListener)
                .setNegativeButton(R.string.cancel, negativeClickListener)
                .setCancelable(true);
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(context, R.color.black_87));
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.fresco_blue));
        });
        dialog.show();
    }

    public static void showFrescoDialog(Context context, @StringRes int title, @StringRes int message, @StringRes int positiveText, DialogInterface.OnClickListener positiveClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(title)
                .setPositiveButton(positiveText, positiveClickListener)
                .setCancelable(true);
        if (message != 0) {
            builder.setMessage(message);
        }
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.fresco_blue));
        });
        dialog.show();
    }

    public static void showFrescoDialog(Context context, CharSequence title, CharSequence message, @StringRes int positiveText, DialogInterface.OnClickListener positiveClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveText, positiveClickListener)
                .setCancelable(true);
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.fresco_blue));
        });
        dialog.show();
    }

    public static void showFrescoDialog(Context context, @StringRes int title, CharSequence message,
                                        @StringRes int positiveText, DialogInterface.OnClickListener positiveClickListener,
                                        @StringRes int negativeText, DialogInterface.OnClickListener negativeClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveText, positiveClickListener)
                .setNegativeButton(negativeText, negativeClickListener)
                .setCancelable(true);
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.fresco_blue));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(context, R.color.black));
        });
        dialog.show();
    }
}
