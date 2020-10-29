package com.fresconews.fresco.v2.gallery.gallerylist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.base.BaseActivity;
import com.fresconews.fresco.framework.persistence.managers.AnalyticsManager;

import java.util.ArrayList;

import javax.inject.Inject;

public class GalleryListActivity extends BaseActivity {

    public final static String TITLE = "title";
    public final static String GALLERY_IDS = "gallery_ids";

    @Inject
    AnalyticsManager analyticsManager;

    public static void start(Context context, String title, ArrayList<String> galleryIds) {
        Intent starter = new Intent(context, GalleryListActivity.class);
        starter.putExtra(GALLERY_IDS, galleryIds);
        starter.putExtra(TITLE, title);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((Fresco2) getApplication()).getFrescoComponent().inject(this);
        setViewModel(R.layout.activity_gallery_list, new GalleryListViewModel(this));
    }

    @Override
    protected void onPause() {
        super.onPause();
        analyticsManager.stopTrackingPost();
    }
}
