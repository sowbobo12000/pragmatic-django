package com.fresconews.fresco.v2.follow;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.base.BaseActivity;

public class FollowActivity extends BaseActivity {

    public static final String USER_ID = "userId";
    public static final String ALT_STATE = "stateInt";

    public static void start(Context context, String userId) {
        Intent starter = new Intent(context, FollowActivity.class);
        starter.putExtra(USER_ID, userId);
        context.startActivity(starter);
    }

    public static void start(Context context, String userId, int state) {
        Intent starter = new Intent(context, FollowActivity.class);
        starter.putExtra(USER_ID, userId);
        starter.putExtra(ALT_STATE, state);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String userId = getIntent().getStringExtra(USER_ID);
        int state = getIntent().getIntExtra(ALT_STATE, 0);
        FollowViewModel viewModel = new FollowViewModel(this, userId, state);
        setViewModel(R.layout.activity_follow, viewModel);
    }
}
