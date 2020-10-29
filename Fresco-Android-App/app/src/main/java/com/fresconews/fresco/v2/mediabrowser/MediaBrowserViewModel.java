package com.fresconews.fresco.v2.mediabrowser;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.databinding.bindingTypes.BindableView;
import com.fresconews.fresco.framework.mvvm.ActivityViewModel;
import com.fresconews.fresco.framework.mvvm.datasources.ListDataSource;
import com.fresconews.fresco.framework.persistence.managers.AnalyticsManager;
import com.fresconews.fresco.v2.submission.SubmissionActivity;
import com.fresconews.fresco.v2.utils.DialogUtils;
import com.fresconews.fresco.v2.utils.LogUtils;
import com.fresconews.fresco.v2.utils.SnackbarUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import io.smooch.ui.ConversationActivity;
import rx.Observable;
import rx.functions.Action1;

public class MediaBrowserViewModel extends ActivityViewModel<MediaBrowserActivity> {

    private static final String TAG = MediaBrowserViewModel.class.getSimpleName();

    @Inject
    AnalyticsManager analyticsManager;

    public BindableView<RecyclerView> mediaList = new BindableView<>();
    public BindableView<View> noContentView = new BindableView<>();
    public BindableView<Button> nextButton = new BindableView<>();
    public BindableView<TextView> bindableTV = new BindableView<>();
    public BindableView<TextView> importantTextView = new BindableView<>();

    private List<MediaItemViewModel> selectedItems;

    public MediaBrowserViewModel(MediaBrowserActivity activity) {
        super(activity);
        ((Fresco2) getActivity().getApplication()).getFrescoComponent().inject(this);
        setTitle(activity.getString(R.string.media_browser_title));
        setNavIcon(R.drawable.ic_navigation_arrow_back_white);

        selectedItems = new ArrayList<>();
    }

    @Override
    public void onBound() {
        super.onBound();

        setMixedStyleImportantText();
        setClickableSpan();

        loadImages().mergeWith(loadVideos())
//                    .filter(mediaItem -> !(mediaItem.getLat() == 0 && mediaItem.getLng() == 0))
                    .toSortedList((lhs, rhs) -> rhs.getDate().compareTo(lhs.getDate()))
                    .onErrorReturn(throwable -> null)
                    .subscribe(mediaItems -> {
                        if (mediaItems == null || mediaItems.isEmpty()) {
                            noContentView.get().setVisibility(View.VISIBLE);
                            mediaList.get().setVisibility(View.GONE);
                        }
                        else {
                            noContentView.get().setVisibility(View.GONE);
                            mediaList.get().setVisibility(View.VISIBLE);

                            for (MediaItemViewModel mediaItem : mediaItems) {
                                mediaItem.onClicked = this::toggleSelection;
                            }
                            // Add fake empty MediaItemViewModel to behave like the footer
                            mediaItems.add(new MediaItemViewModel());

                            ListDataSource<MediaItemViewModel> dataSource = new ListDataSource<>(mediaItems);
                            MediaBrowserRecyclerViewAdapter adapter = new MediaBrowserRecyclerViewAdapter(dataSource);

                            GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 3);
                            layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                                @Override
                                public int getSpanSize(int position) {
                                    return adapter.isFooter(position) ? layoutManager.getSpanCount() : 1;
                                }
                            });

                            mediaList.get().setLayoutManager(layoutManager);
                            mediaList.get().setAdapter(adapter);
                        }
                    });
    }

    public Action1<View> goBack = view -> {
        getActivity().onBackPressed();
    };

    private void toggleSelection(MediaItemViewModel item, boolean overTimeLimit) {
        if (overTimeLimit) {
            DialogUtils.showFrescoDialog(getActivity(), R.string.videos_must_be_one_minute, 0, android.R.string.ok,
                    (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    });
            return;
        }

        if (selectedItems.contains(item)) {
            selectedItems.remove(item);
        }
        else {

            selectedItems.add(item);
        }

        if (selectedItems.size() > 0 && selectedItems.size() <= 8) {
            nextButton.get().setEnabled(true);
            nextButton.get().setTextColor(ContextCompat.getColor(getActivity(), R.color.fresco_blue));
        }
        else {
            if (selectedItems.size() > 0) {
                SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_pick_more_than_8);
            }
            nextButton.get().setEnabled(false);
            nextButton.get().setTextColor(ContextCompat.getColor(getActivity(), R.color.black_54));
        }
    }

    public Action1<View> nextButtonClicked = view -> {
        ArrayList<MediaItemViewModel> mediaItems = new ArrayList<>(selectedItems);
        SubmissionActivity.start(getActivity(), mediaItems);
    };

    private Observable<MediaItemViewModel> loadImages() {
        return Observable.create(subscriber -> {
            ContentResolver contentResolver = getActivity().getContentResolver();
            Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

            Cursor cursor = contentResolver.query(imageUri, null, null, null, null);

            if (cursor == null) {
                LogUtils.e(TAG, "Error querying images");
                subscriber.onError(new RuntimeException("Error querying media store for images"));
            }
            else if (!cursor.moveToFirst()) {
                LogUtils.i(TAG, "No images returned");
                subscriber.onCompleted();
            }
            else {
                LogUtils.i(TAG, "Loading images");
                int idColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                int latColumn = cursor.getColumnIndex(MediaStore.Images.Media.LATITUDE);
                int lngColumn = cursor.getColumnIndex(MediaStore.Images.Media.LONGITUDE);
                int mimeColumn = cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE);
                int dateColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);

                do {
                    MediaItemViewModel item = new MediaItemViewModel();

                    item.setLat(cursor.getDouble(latColumn));
                    item.setLng(cursor.getDouble(lngColumn));
                    item.setMimeType(cursor.getString(mimeColumn));
                    item.setDate(new Date(cursor.getLong(dateColumn)));

                    long id = cursor.getLong(idColumn);

                    item.setUri(ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id));
                    subscriber.onNext(item);

                } while (cursor.moveToNext());
                cursor.close();

                subscriber.onCompleted();
            }
        }).cast(MediaItemViewModel.class).filter(mediaItem -> mediaItem.getMimeType().equals("image/jpeg"));
    }

    private Observable<MediaItemViewModel> loadVideos() {
        return Observable.create(subscriber -> {
            ContentResolver contentResolver = getActivity().getContentResolver();
            Uri videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

            Cursor cursor = contentResolver.query(videoUri, null, null, null, null);

            if (cursor == null) {
                LogUtils.e(TAG, "Error querying videos");
                subscriber.onError(new RuntimeException("Error querying media store for videos"));
            }
            else if (!cursor.moveToFirst()) {
                LogUtils.i(TAG, "No videos returned");
                subscriber.onCompleted();
            }
            else {
                LogUtils.i(TAG, "Loading videos");
                int idColumn = cursor.getColumnIndex(MediaStore.Video.Media._ID);
                int latColumn = cursor.getColumnIndex(MediaStore.Video.Media.LATITUDE);
                int lngColumn = cursor.getColumnIndex(MediaStore.Video.Media.LONGITUDE);
                int mimeColumn = cursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE);
                int dateColumn = cursor.getColumnIndex(MediaStore.Video.Media.DATE_TAKEN);
                int durationColumn = cursor.getColumnIndex(MediaStore.Video.Media.DURATION);

                do {
                    MediaItemViewModel item = new MediaItemViewModel();

                    item.setLat(cursor.getDouble(latColumn));
                    item.setLng(cursor.getDouble(lngColumn));
                    item.setMimeType(cursor.getString(mimeColumn));
                    item.setDate(new Date(cursor.getLong(dateColumn)));

                    long id = cursor.getLong(idColumn);
                    item.setUri(ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id));

                    long duration = cursor.getLong(durationColumn);

                    item.setDuration(duration);

                    subscriber.onNext(item);

                } while (cursor.moveToNext());

                cursor.close();
                subscriber.onCompleted();
            }
        }).cast(MediaItemViewModel.class).filter(mediaItem -> mediaItem.getMimeType().equals("video/mp4"));
    }

    public Action1<View> chatWithUs = view -> {
        ConversationActivity.show(getActivity());
    };

    private void setMixedStyleImportantText() {
        String s1 = getString(R.string.missing_content_important_1);
        String s2 = getString(R.string.missing_content_important_2);
        String s3 = getString(R.string.missing_content_important_3);
        String fullString = String.format(Locale.getDefault(), "%s %s %s", s1, s2, s3);

        Spannable spannableText = new SpannableString(fullString);
        spannableText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.fresco_red)), 0, s1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.black_87)), s1.length() + 1, fullString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableText.setSpan(new StyleSpan(Typeface.BOLD), 0, s1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableText.setSpan(new StyleSpan(Typeface.NORMAL), s1.length() + 1, s1.length() + s2.length() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableText.setSpan(new StyleSpan(Typeface.BOLD), s1.length() + s2.length() + 2, fullString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        importantTextView.get().setText(spannableText);
    }

    private void setClickableSpan() {
        String string = getActivity().getResources().getString(R.string.missing_content);
        SpannableString ss = new SpannableString(string);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                Intent viewIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                getActivity().startActivity(viewIntent);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(ContextCompat.getColor(getActivity(), R.color.black_87));
                ds.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                ds.setUnderlineText(false);
            }
        };
        ss.setSpan(clickableSpan, 112, 139, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        bindableTV.get().setText(ss);
        bindableTV.get().setMovementMethod(LinkMovementMethod.getInstance());
    }
}
