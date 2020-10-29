package com.fresconews.fresco.v2.submission;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.base.BaseActivity;
import com.fresconews.fresco.framework.persistence.managers.AnalyticsManager;
import com.fresconews.fresco.framework.persistence.managers.FrescoLocationManager;
import com.fresconews.fresco.v2.mediabrowser.MediaItemViewModel;
import com.fresconews.fresco.v2.utils.LogUtils;

import java.util.ArrayList;

import javax.inject.Inject;

import pl.charmas.android.reactivelocation.ReactiveLocationProvider;

public class SubmissionActivity extends BaseActivity {
    public static final String SUBMISSIONS = "submissions";

    private EditText captionEditText;
    private boolean textChangeStarted = false;

    @Inject
    AnalyticsManager analyticsManager;

    @Inject
    FrescoLocationManager locationManager;

    public static void start(Context context, ArrayList<MediaItemViewModel> mediaItems) {
        Intent starter = new Intent(context, SubmissionActivity.class);
        starter.putParcelableArrayListExtra(SUBMISSIONS, mediaItems);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((Fresco2) getApplication()).getFrescoComponent().inject(this);
        String captionText = "";
        ArrayList<MediaItemViewModel> mediaItems = getIntent().getParcelableArrayListExtra(SUBMISSIONS);

        for (MediaItemViewModel mediaItem : mediaItems) {
            LogUtils.i("submishhh", "Lat: " + Double.toString(mediaItem.getLat()));
            LogUtils.i("submishhh", "Date: " + mediaItem.getDate().toString());
        }

        analyticsManager.trackScreen("Submission");

        MediaItemViewModel captionItem = mediaItems.get(mediaItems.size() - 1); // possible caption item coming from singup/login
        if (captionItem.getDuration() == -13) {
            mediaItems.remove(mediaItems.size() - 1);
            captionText = captionItem.getMimeType();
        }

        setViewModel(R.layout.activity_submission, new SubmissionViewModel(this, mediaItems));
        if (locationManager.getReactiveLocationProvider() == null) {
            locationManager.setReactiveLocationProvider(new ReactiveLocationProvider(this));
        }

        //Imogen would like us to measure the amount of time it took for a user to write a caption
        captionEditText = (EditText) findViewById(R.id.caption_edit_text);
        String finalCaptionText = captionText;
        captionEditText.post(() -> {
            captionEditText.append(finalCaptionText);
        });
        captionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!textChangeStarted) {
                    textChangeStarted = true;
                    analyticsManager.startedWritingSubmissionCaption();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

}
