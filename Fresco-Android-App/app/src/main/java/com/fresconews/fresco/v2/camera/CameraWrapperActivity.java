package com.fresconews.fresco.v2.camera;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Maurice Wu on 07/10/2016.
 */
public class CameraWrapperActivity extends AppCompatActivity {

    public static void start(Context context) {
        Intent starter = new Intent(context, CameraWrapperActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startActivityForResult(new Intent(this, CameraActivity.class), 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) {
            finish();
        }
        else if (resultCode == RESULT_OK) {
            CameraActivity.start(this, data.getBooleanExtra(CameraActivity.EXTRA_VIDEO_MODE, false));
        }
    }
}
