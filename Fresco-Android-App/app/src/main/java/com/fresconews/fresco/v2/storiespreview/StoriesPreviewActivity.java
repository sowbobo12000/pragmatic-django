package com.fresconews.fresco.v2.storiespreview;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.base.BaseActivity;
import com.fresconews.fresco.framework.persistence.managers.AnalyticsManager;
import com.fresconews.fresco.framework.persistence.managers.SessionManager;
import com.fresconews.fresco.framework.persistence.models.SearchQuery;
import com.fresconews.fresco.v2.storygallery.StoryGalleryActivity;
import com.google.gson.Gson;

import javax.inject.Inject;

import static com.fresconews.fresco.v2.search.SearchActivity.EXTRA_FROM_SEARCH;
import static com.fresconews.fresco.v2.search.SearchActivity.EXTRA_QUERY;

/**
 * Created by Khalid P on 4/7/16.
 */
public class StoriesPreviewActivity extends BaseActivity {

    public static final String EXTRA_ACTION_FROM_NOTIFICATION = "EXTRA_ACTION_FROM_NOTIFICATION";

    @Inject
    SessionManager sessionManager;

    @Inject
    AnalyticsManager analyticsManager;

    public static void start(Context context) {
        Intent starter = new Intent(context, StoriesPreviewActivity.class);
        context.startActivity(starter);
    }

    public static void start(Context context, SearchQuery searchQuery, boolean newTask, boolean fromSearch) {
        Intent starter = new Intent(context, StoriesPreviewActivity.class);
        if (newTask) {
            starter.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        starter.putExtra(EXTRA_QUERY, new Gson().toJson(searchQuery, SearchQuery.class));
        starter.putExtra(EXTRA_FROM_SEARCH, fromSearch);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((Fresco2) getApplication()).getFrescoComponent().inject(this);

        boolean fromSearch = getIntent().getBooleanExtra(EXTRA_FROM_SEARCH, false);
        String searchQueryString = getIntent().getStringExtra(EXTRA_QUERY);
        SearchQuery searchQuery = new Gson().fromJson(searchQueryString, SearchQuery.class);

        StoriesPreviewViewModel viewModel = new StoriesPreviewViewModel(this, fromSearch, searchQuery);

        if (!fromSearch) {
            analyticsManager.storiesScreenOpened();
        }

        setViewModel(R.layout.activity_stories_preview, viewModel);

        if (getIntent().getBooleanExtra(EXTRA_ACTION_FROM_NOTIFICATION, false)) {
            StoryGalleryActivity.start(this, getIntent().getExtras());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        analyticsManager.storiesScreenClosed();
    }
}
