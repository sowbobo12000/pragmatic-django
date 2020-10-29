package com.fresconews.fresco.v2.gallery.gallerydetail;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.NestedScrollView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.base.BaseActivity;
import com.fresconews.fresco.framework.persistence.managers.AnalyticsManager;
import com.fresconews.fresco.framework.persistence.managers.GalleryManager;
import com.fresconews.fresco.v2.utils.LogUtils;

import org.json.JSONArray;

import java.util.Date;

import javax.inject.Inject;

public class GalleryDetailActivity extends BaseActivity {
    private static final String TAG = GalleryDetailActivity.class.getSimpleName();

    @Inject
    AnalyticsManager analyticsManager;

    @Inject
    GalleryManager galleryManager;

    public static final String EXTRA_GALLERY_ID = "gallery_id";
    public static final String EXTRA_CURRENT_POST = "current_post";
    public static final String EXTRA_CURRENT_POST_POSITION = "EXTRA_CURRENT_POST_POSITION";
    public static final String EXTRA_CURRENT_COMMENT_ID = "current_comment_id";

    private int scrollYThreshold = 1234567; // set to ridiculously high number.
    //once the comments section comes into view, the threshold is anchored to this position.
    //this helps keep "ADD A COMMENT" displayed as the comments section leaves the view.
    //imogen was very specific for the button to change when you enter the comments section, not
    //when you view the first comment.

    private boolean foundTheCommentsSection = false;
    private boolean referToCoordinator = true;
    private String galleryId;
    private String[] tags = null;
    private int percentScrolled = -1;
    private int maxScrolledHeight;

    private GalleryDetailViewModel viewModel;

    public static void start(Context context, Bundle extras) {
        Intent starter = new Intent(context, GalleryDetailActivity.class);
        starter.putExtras(extras);
        context.startActivity(starter);
    }

    public static void start(Context context, String galleryId, int currentPost, Bundle options) {
        Intent starter = new Intent(context, GalleryDetailActivity.class);
        starter.putExtra(GalleryDetailActivity.EXTRA_GALLERY_ID, galleryId);
        starter.putExtra(GalleryDetailActivity.EXTRA_CURRENT_POST_POSITION, currentPost);
        context.startActivity(starter, options);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((Fresco2) getApplication()).getFrescoComponent().inject(this);

        Fresco2.activePostIdInView = null;

        String currentPost = null;
        String currentCommentUserId = null;
        int currentPostPosition = 0;

        analyticsManager.trackScreen("Gallery Detail");
        analyticsManager.galleryOpened();

        String scheme = getIntent().getScheme();
        if (getIntent().getData() != null && !TextUtils.isEmpty(scheme) && scheme.startsWith("http")) {
            galleryId = getIntent().getData().getLastPathSegment();
        }
        else {
            galleryId = getIntent().getStringExtra(EXTRA_GALLERY_ID);
            currentPost = getIntent().getStringExtra(EXTRA_CURRENT_POST);
            currentCommentUserId = getIntent().getStringExtra(EXTRA_CURRENT_COMMENT_ID);
            currentPostPosition = getIntent().getIntExtra(EXTRA_CURRENT_POST_POSITION, 0);

            LogUtils.i(TAG, "galleryId - " + galleryId);
            LogUtils.i(TAG, "currentPost - " + currentPost);
        }

        viewModel = new GalleryDetailViewModel(this, galleryId, currentCommentUserId, currentPost, currentPostPosition);
        setViewModel(R.layout.activity_gallery_detail, viewModel);

        galleryManager.downloadNetworkGallery(galleryId)
                      .onErrorReturn(throwable -> null)
                      .subscribe(networkGallery -> {
                          if (networkGallery != null) {
                              tags = networkGallery.getTags();
                          }
                      });

        NestedScrollView scroller = (NestedScrollView) findViewById(R.id.myScroll);
        TextView commentsTitleTextView = (TextView) findViewById(R.id.comments_title_textview);
        Rect scrollBounds = new Rect();
        scroller.getHitRect(scrollBounds);

        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.gallery_detail_root);

        Rect coorScrollBounds = new Rect();
        coordinatorLayout.getHitRect(coorScrollBounds);
        new CountDownTimer(600000, 50) {
            public void onTick(long millisUntilFinished) {
                if (!referToCoordinator) {
                    return;
                }

                if (commentsTitleTextView.getLocalVisibleRect(coorScrollBounds)) {
                    viewModel.getCommentsViewModel().setAddCommentButtonText(true);
                }
                else {
                    viewModel.getCommentsViewModel().setAddCommentButtonText(false);
                }
            }

            public void onFinish() {
            }
        }.start();

        scroller.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                maxScrolledHeight = v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight();
                viewModel.getCommentsViewModel().hideCommentBottomSheet();  // if user starts scrolling, close the delete drawer

                if (commentsTitleTextView.getLocalVisibleRect(coorScrollBounds) || scrollY >= scrollYThreshold) {
                    // Any portion of the imageView, even a single pixel, is within the visible window
                    referToCoordinator = false;
                    viewModel.getCommentsViewModel().setAddCommentButtonText(true);
                    if (!foundTheCommentsSection) {
                        scrollYThreshold = scrollY;
                        foundTheCommentsSection = true;
                    }
                }
                else {
                    // NONE of the imageView is within the visible window
                    viewModel.getCommentsViewModel().setAddCommentButtonText(false);
                }

                if (scrollY == 0) {
                    referToCoordinator = true;
                }
                int tmpPercentScrolled = (int) ((double) scrollY * 100 / (double) maxScrolledHeight);
                if (scrollY > maxScrolledHeight) {
                    tmpPercentScrolled = 100;
                }
                if (tmpPercentScrolled > percentScrolled) {
                    percentScrolled = tmpPercentScrolled;
                }

                if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                    viewModel.getCommentsViewModel().loadNewerComments(); //triggering pagination at bottom
                    v.getChildAt(0).getMeasuredHeight();
                    v.getMeasuredHeight();
                    percentScrolled = 100;
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        analyticsManager.stopTrackingPost();
        String author = getGalleryOwnerId();
        Date highlightedAt = getHighlightedAt();
        JSONArray jsonTags = new JSONArray();

        if (tags != null) {
            for (String tag : tags) {
                jsonTags.put(tag);
            }
        }
        if (percentScrolled == -1) {
            percentScrolled = 0;
        }

        analyticsManager.galleryClosed(galleryId, author, percentScrolled, highlightedAt, jsonTags);
    }

    @Override
    public void onBackPressed() {
        if (!viewModel.getCommentsViewModel().isCommentBottomSheetHidden()) {
            viewModel.getCommentsViewModel().hideCommentBottomSheet();
            return;
        }

        if (viewModel.getCommentsViewModel().commentEditText.get().getVisibility() == View.VISIBLE) {
            viewModel.getCommentsViewModel().hideEditText();
            return;
        }

        super.onBackPressed();
    }

    public String getGalleryOwnerId() {
        return viewModel.getGalleryOwnerId();
    }

    private Date getHighlightedAt() {
        return viewModel.getHighlightedAt();
    }
}
