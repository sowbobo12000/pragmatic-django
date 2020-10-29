package com.fresconews.fresco.framework.mvvm.viewmodels;

import android.app.Activity;
import android.databinding.Bindable;
import android.graphics.Typeface;
import android.support.annotation.PluralsRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.fresconews.fresco.BR;
import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.databinding.bindingTypes.BindableView;
import com.fresconews.fresco.framework.mvvm.ItemViewModel;
import com.fresconews.fresco.framework.persistence.managers.AnalyticsManager;
import com.fresconews.fresco.framework.persistence.managers.GalleryManager;
import com.fresconews.fresco.framework.persistence.managers.SessionManager;
import com.fresconews.fresco.framework.persistence.managers.UserManager;
import com.fresconews.fresco.framework.persistence.models.Comment;
import com.fresconews.fresco.framework.persistence.models.CommentEntity;
import com.fresconews.fresco.framework.mvvm.datasources.IDataSource;
import com.fresconews.fresco.framework.persistence.models.User;
import com.fresconews.fresco.v2.gallery.gallerydetail.GalleryDetailActivity;
import com.fresconews.fresco.v2.gallery.gallerydetail.GalleryDetailViewModel;
import com.fresconews.fresco.v2.profile.ProfileActivity;
import com.fresconews.fresco.v2.report.ReportBottomSheetDialogFragment;
import com.fresconews.fresco.v2.report.ReportBottomSheetDialogViewModel;
import com.fresconews.fresco.v2.report.ReportUserDialogViewModel;
import com.fresconews.fresco.v2.search.SearchActivity;
import com.fresconews.fresco.v2.utils.DialogUtils;
import com.fresconews.fresco.v2.utils.LogUtils;
import com.google.gson.Gson;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.Seconds;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Blaze on 8/25/2016.
 */
public class CommentViewModel extends ItemViewModel<Comment> {
    private static final String TAG = CommentViewModel.class.getSimpleName();

    @Inject
    SessionManager sessionManager;

    @Inject
    UserManager userManager;

    @Inject
    GalleryManager galleryManager;

    @Inject
    AnalyticsManager analyticsManager;

    private WeakReference<Activity> activity;
    public BindableView<TextView> bindableCommentTV = new BindableView<>();
    private ReportBottomSheetDialogFragment reportFragment;

    private static final int HASHTAG_TYPE = 0;
    private static final int MENTIONS_TYPE = 1;
    private String commentText;
    private Comment comment;
    private String username;
    private Date updatedAt;

    public CommentViewModel(Activity activity, Comment item) {

        super(item);
        ((Fresco2) activity.getApplication()).getFrescoComponent().inject(this);

        this.activity = new WeakReference<>(activity);
        this.commentText = item.getComment();
        this.comment = item;
        this.username = comment.getUsername();
        this.updatedAt = comment.getUpdatedAt();
        setAvatarUrl(comment.getAvaterUrl());

    }

    @Override
    public void onBound() {
        if (hasBeenBound) {
            return;
        }

        super.onBound();

        ArrayList<Integer> specialSpanIndexes = new ArrayList<>();

        // We need to bind the comment entites (link to searches and text)
        SpannableString ss = new SpannableString(commentText);

        // First we need to get all the comment entities from the DB
        IDataSource<CommentEntity> commentEntites = galleryManager.getCommentsEntities(comment.getId());

        for (int i = 0; i < commentEntites.getItemCount(); i++) {
            CommentEntity entity = commentEntites.get(i);

            if (entity.getEntityType().equals("tag")) {
                String searchTerm = entity.getText();

                ClickableSpan clickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(View view) {
                        SearchActivity.start(view.getContext(), searchTerm, false, false);
                    }

                    @Override
                    public void updateDrawState(TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setColor(ContextCompat.getColor(activity.get(), R.color.black_87));
                        ds.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                        ds.setUnderlineText(false);
                    }
                };
                ss.setSpan(clickableSpan, entity.getStartIndex(), entity.getEndIndex() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            if (entity.getEntityType().equals("user")) {
                String username = entity.getText();
                String userId = entity.getEntityId();

                ClickableSpan clickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(View view) {
                        userManager.downloadUser(userId)
                                   .onErrorReturn(throwable -> null)
                                   .subscribeOn(Schedulers.io())
                                   .subscribe(user -> {
                                       if (user != null) {
                                           ProfileActivity.start(view.getContext(), user.getId());
                                       }
                                   });
                    }

                    @Override
                    public void updateDrawState(TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setColor(activity.get().getResources().getColor(R.color.black_87));
                        ds.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                        ds.setUnderlineText(false);
                    }
                };
                ss.setSpan(clickableSpan, entity.getStartIndex(), entity.getEndIndex() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                specialSpanIndexes.add(entity.getStartIndex());
                specialSpanIndexes.add(entity.getEndIndex() + 1);
            }
        }

        //NOW WE WANT THE REMAINDER OF THE TEXT TO BE A LINK TO COMMENT @ USER
        //We'll use this for non # and @'s
        ClickableSpan regularTextSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                commentAtUser.call(null);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(ContextCompat.getColor(activity.get(), R.color.black_87));
                ds.setUnderlineText(false);
            }
        };

        //If there were no tags, make the whole comment text link to @ user
        if (specialSpanIndexes.size() == 0) {
            ss.setSpan(regularTextSpan, 0, commentText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        //Fill remaining clickable span for comment at user
        int startRegularSpanHere = 0;
        for (int i = 0; i < specialSpanIndexes.size() - 1; i += 2) {
            int takenIndexStart = specialSpanIndexes.get(i);
            int takenIndexEnd = specialSpanIndexes.get(i + 1);

            //Take care of back to back tags and tags at beginnings of comment text
            if (startRegularSpanHere == takenIndexStart) {
                startRegularSpanHere = takenIndexEnd + 1;

                //if this is the last tag tho:
                if (i + 2 == specialSpanIndexes.size() && startRegularSpanHere < commentText.length()) {
                    ss.setSpan(regularTextSpan, startRegularSpanHere, commentText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                continue;
            }

            //We'll use this for non # and @'s
            ClickableSpan anotherRegularSpan = new ClickableSpan() { //need one obj for each span
                @Override
                public void onClick(View view) {
                    commentAtUser.call(null);
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(ContextCompat.getColor(activity.get(), R.color.black_87));
                    ds.setUnderlineText(false);
                }
            };

            try {
                ss.setSpan(anotherRegularSpan, startRegularSpanHere, takenIndexStart - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                startRegularSpanHere = takenIndexEnd + 1;

                //Take care of last tag in comment text view
                if (i + 2 == specialSpanIndexes.size() && startRegularSpanHere < commentText.length()) {
                    ss.setSpan(regularTextSpan, startRegularSpanHere, commentText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            catch (Exception e) {
                //Can't track down a spanning error, will log here so i can find easier when it happens todo remove in 3.1.4 or 1.5 if comment error isn't happening
                analyticsManager.screenDebug("gallery_detail", "Span error - " + e.getMessage() + "\nWith comment - " + commentText, comment.getGalleryId(), comment.getId());
            }
        }

        bindableCommentTV.get().setText(ss);
        bindableCommentTV.get().setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Bindable
    public String getExpirationString() {
        if (comment == null || comment.getUpdatedAt() == null) {
            LogUtils.i(TAG, "null comment");
            return "";
        }

        int days = Days.daysBetween(new DateTime(getUpdatedAt()), new DateTime()).getDays();
        int hours = Hours.hoursBetween(new DateTime(getUpdatedAt()), new DateTime()).getHours();
        int minutes = Minutes.minutesBetween(new DateTime(getUpdatedAt()), new DateTime()).getMinutes();
        int seconds = Seconds.secondsBetween(new DateTime(getUpdatedAt()), new DateTime()).getSeconds();

        if (days > 0) {
            return getQuantityString(R.plurals.days, days, days) + " ago";
        }
        if (hours > 0) {
            return getQuantityString(R.plurals.hours, hours, hours) + " ago";
        }
        if (minutes > 0) {
            return getQuantityString(R.plurals.minutes, minutes, minutes) + " ago";
        }
        if (seconds > 0) {
            return getQuantityString(R.plurals.seconds, seconds, seconds) + " ago";
        }
        if (seconds == 0) {
            return "Just now";
        }
        LogUtils.i(TAG, "nothing got triggered - " + Integer.toString(days) + " " + Integer.toString(hours) + " " + Integer.toString(minutes) + " " + Integer.toString(seconds));

        return "";
    }

    protected String getString(@StringRes int stringRes, Object... formatArgs) {
        if (activity == null || stringRes == 0) {
            return "";
        }
        return activity.get().getString(stringRes, formatArgs);
    }

    protected String getQuantityString(@PluralsRes int pluralRes, int quantity, Object... formatArgs) {
        if (activity == null || pluralRes == 0) {
            return "";
        }
        return activity.get().getResources().getQuantityString(pluralRes, quantity, formatArgs);
    }

    @Bindable
    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Bindable
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Bindable
    public String getAvatarUrl() {
        return getItem().getAvaterUrl();
    }

    public void setAvatarUrl(String avatarUrl) {
        getItem().setAvaterUrl(avatarUrl);
    }

    @Bindable
    public String getComment() {
        return getItem().getComment();
    }

    public Action1<View> viewUser = view -> {
        ProfileActivity.start(view.getContext(), getItem().getUserId(), false, true, false);
    };

    public Action1<View> commentAtUser = view -> {
        GalleryDetailActivity galleryDetailActivity = (GalleryDetailActivity) activity.get();
        ((GalleryDetailViewModel) galleryDetailActivity.getViewModel()).getCommentsViewModel().addComment("@" + getItem().getUsername());
    };

    public Action1<View> viewDeleteOrReport = view -> {
        //If not logged in
        if (sessionManager == null || sessionManager.getCurrentSession() == null ||
                !sessionManager.isLoggedIn() ||
                sessionManager.getCurrentSession().getUserId() == null) {
            return;
        }

        GalleryDetailActivity galleryDetailActivity = (GalleryDetailActivity) activity.get();
        String userId = sessionManager.getCurrentSession().getUserId(); // of current user
        String commentId = getItem().getId();
        boolean myComment = userId.equals(getItem().getUserId());
        Boolean myGallery = userId.equals(galleryDetailActivity.getGalleryOwnerId());

        //If this is your comment on your gallery
        ((GalleryDetailViewModel) galleryDetailActivity.getViewModel()).showCommentOptions(getItem().getUserId(), commentId, myGallery, myComment);
    };

    public String getId() {
        return getItem().getId();
    }

    public Date getCreatedAt() {
        return getItem().getCreatedAt();
    }

    public int getPosition() {
        return getItem().getPosition();
    }

}
