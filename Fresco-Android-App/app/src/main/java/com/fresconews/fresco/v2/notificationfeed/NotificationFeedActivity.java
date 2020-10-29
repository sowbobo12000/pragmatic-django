package com.fresconews.fresco.v2.notificationfeed;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.base.BaseActivity;

/**
 * Created by Maurice on 30/08/2016.
 */
public class NotificationFeedActivity extends BaseActivity {

    public static void start(Context context) {
        Intent starter = new Intent(context, NotificationFeedActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NotificationFeedViewModel viewModel = new NotificationFeedViewModel(this);
        setViewModel(R.layout.activity_notification_feed, viewModel);
    }
}
