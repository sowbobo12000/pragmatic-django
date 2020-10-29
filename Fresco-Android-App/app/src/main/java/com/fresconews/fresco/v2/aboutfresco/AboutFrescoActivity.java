package com.fresconews.fresco.v2.aboutfresco;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.base.BaseActivity;

/**
 * Created by Khalid P on 8/31/2016.
 */
public class AboutFrescoActivity extends BaseActivity {

    public static void start(Context context) {
        Intent starter = new Intent(context, AboutFrescoActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AboutFrescoViewModel viewModel = new AboutFrescoViewModel(this);
        setViewModel(R.layout.activity_about_fresco, viewModel);
    }
}
