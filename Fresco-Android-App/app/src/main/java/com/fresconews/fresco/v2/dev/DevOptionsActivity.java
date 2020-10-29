package com.fresconews.fresco.v2.dev;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.base.BaseActivity;
import com.fresconews.fresco.v2.home.HomeActivity;

import java.util.Random;

/**
 * Created by mauricewu on 11/14/16.
 */
public class DevOptionsActivity extends BaseActivity {
    private DevOptionsViewModel viewModel;

    public static void start(Context context) {
        Intent starter = new Intent(context, DevOptionsActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new DevOptionsViewModel(this);
        setViewModel(R.layout.activity_dev_options, viewModel);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (viewModel.needsRestart()) {
            Intent intent = new Intent(this, HomeActivity.class);
            int intentId = new Random().nextInt();
            PendingIntent pendingIntent = PendingIntent.getActivity(this, intentId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 10, pendingIntent);
            System.exit(0);
        }
    }
}
