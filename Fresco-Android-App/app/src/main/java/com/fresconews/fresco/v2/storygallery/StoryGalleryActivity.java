package com.fresconews.fresco.v2.storygallery;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.base.BaseActivity;
import com.fresconews.fresco.framework.persistence.managers.AnalyticsManager;
import com.fresconews.fresco.framework.persistence.managers.SessionManager;

import javax.inject.Inject;

/**
 * Created by techjini on 11/7/16.
 */
public class StoryGalleryActivity extends BaseActivity {
    public static final String EXTRA_STORY_TITLE = "ipc_key_story_title";
    public static final String EXTRA_STORY_ID = "ipc_key_story_id";
    public static final String EXTRA_STORY_CAPTION = "ipc_key_story_caption";

    @Inject
    SessionManager sessionManager;

    @Inject
    AnalyticsManager analyticsManager;

    public static void start(Context context, Bundle extras) {
        Intent starter = new Intent(context, StoryGalleryActivity.class);
        starter.putExtras(extras);
        context.startActivity(starter);
    }

    public static void start(Context context, String id, String title, String caption) {
        start(context, id, title, caption, false);
    }

    public static void start(Context context, String id, String title, String caption, boolean newTask) {
        Intent starter = new Intent(context, StoryGalleryActivity.class);
        starter.putExtra(StoryGalleryActivity.EXTRA_STORY_TITLE, title);
        starter.putExtra(StoryGalleryActivity.EXTRA_STORY_ID, id);
        starter.putExtra(StoryGalleryActivity.EXTRA_STORY_CAPTION, caption);
        if (newTask) {
            starter.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((Fresco2) getApplication()).getFrescoComponent().inject(this);

        String title = getIntent().getStringExtra(EXTRA_STORY_TITLE);
        String id = getIntent().getStringExtra(EXTRA_STORY_ID);
        String caption = getIntent().getStringExtra(EXTRA_STORY_CAPTION);

        analyticsManager.trackScreen("Story Detail");

        StoryGalleryViewModel viewModel = new StoryGalleryViewModel(this, title, id, caption);
        setViewModel(R.layout.activity_story_gallery, viewModel);
    }

    @Override
    protected void onPause() {
        super.onPause();
        analyticsManager.stopTrackingPost();
    }
}
