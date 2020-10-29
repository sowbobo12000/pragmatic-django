package com.fresconews.fresco.v2.assignments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.base.BaseActivity;
import com.fresconews.fresco.v2.utils.LogUtils;

public class GlobalAssignmentActivity extends BaseActivity {
    public static final String EXPIRED_ASSIGNMENT = "expiredAssignment";

    public static void start(Context context) {
        Intent starter = new Intent(context, GlobalAssignmentActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        boolean expiredAssignment = false;

        if(intent!= null){
            expiredAssignment = intent.getBooleanExtra(EXPIRED_ASSIGNMENT, false);
        }
        GlobalAssignmentViewModel viewModel = new GlobalAssignmentViewModel(this, expiredAssignment);
        setViewModel(R.layout.activity_global_assignment, viewModel);
    }
}
