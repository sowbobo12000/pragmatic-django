package com.fresconews.fresco.v2.mediabrowser;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.base.BaseActivity;
import com.fresconews.fresco.v2.camera.CameraWrapperActivity;

public class MediaBrowserActivity extends BaseActivity {

    public static void start(Context context) {
        Intent starter = new Intent(context, MediaBrowserActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setViewModel(R.layout.activity_media_browser, new MediaBrowserViewModel(this));
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        CameraWrapperActivity.start(this);
    }
}
