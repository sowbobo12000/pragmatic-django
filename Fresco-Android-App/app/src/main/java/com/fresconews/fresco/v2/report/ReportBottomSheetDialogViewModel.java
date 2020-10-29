package com.fresconews.fresco.v2.report;

import android.app.Activity;
import android.databinding.Bindable;
import android.view.View;

import com.fresconews.fresco.BR;
import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.mvvm.ViewModel;
import com.fresconews.fresco.framework.persistence.managers.UserManager;
import com.fresconews.fresco.framework.persistence.models.Gallery;
import com.fresconews.fresco.framework.persistence.models.User;
import com.fresconews.fresco.v2.utils.LogUtils;

import javax.inject.Inject;

import rx.functions.Action1;

/**
 * Created by mauricewu on 11/18/16.
 */
public class ReportBottomSheetDialogViewModel extends ViewModel {
    @Inject
    UserManager userManager;

    private OnReportAndBlockListener listener;
    private String reportUserText;
    private String blockUserText;
    private boolean following;
    private boolean blocking;
    private boolean blocked;
    private boolean fromComment = false;
    private boolean myGallery = false;
    private boolean myComment = false;
    private boolean noOwner = false;

    private boolean reportingGallery;

    public ReportBottomSheetDialogViewModel(Activity activity, Gallery gallery, OnReportAndBlockListener listener) {
        ((Fresco2) activity.getApplication()).getFrescoComponent().inject(this);
        this.listener = listener;

        if (gallery != null) {
            userManager.getOrDownloadUser(gallery.getOriginalOwnerId())
                       .onErrorReturn(throwable -> null)
                       .subscribe(user -> {
                           if (user == null) {
                               setNoOwner(true); //  to get rid of Report user/Block user
                           }
                           else {
                               setReportUserText(activity.getString(R.string.report_user, user.getUsername()));
                               refreshBlockText(activity, user);
                           }
                       });
        }
        setReportingGallery(true);
    }

    public ReportBottomSheetDialogViewModel(Activity activity, User user, OnReportAndBlockListener listener) {
        ((Fresco2) activity.getApplication()).getFrescoComponent().inject(this);
        this.listener = listener;

        setFollowing(user.isFollowing());
        setReportUserText(activity.getString(R.string.report_user, user.getUsername()));
        refreshBlockText(activity, user);
        setReportingGallery(false);
    }

    public ReportBottomSheetDialogViewModel(Activity activity, User user, OnReportAndBlockListener listener, boolean fromComment, boolean myGallery, boolean myComment) {
        ((Fresco2) activity.getApplication()).getFrescoComponent().inject(this);
        this.listener = listener;

        setFollowing(user.isFollowing());
        setReportUserText(activity.getString(R.string.report_user, user.getUsername()));
        refreshBlockText(activity, user);
        setReportingGallery(false);
        setFromComment(fromComment);
        setMyComment(myComment);
        setMyGallery(myGallery);
    }

    @Override
    public void onBound() {
        super.onBound();
    }

    public void refreshBlockText(Activity activity, String userId) {
        userManager.getOrDownloadUser(userId)
                   .onErrorReturn(throwable -> null)
                   .subscribe(user -> {
                       if (user != null) {
                           refreshBlockText(activity, user);
                       }
                   });
    }

    public void refreshBlockText(Activity activity, User user) {
        if (user.isBlocking()) {
            setBlockUserText(activity.getString(R.string.unblock_user, user.getUsername()));
            setBlocking(false);
        }
        else {
            setBlockUserText(activity.getString(R.string.block_user, user.getUsername()));
            setBlocking(true);
        }
        setBlocked(user.isBlocked());
    }

    @Bindable
    public boolean isBlocking() {
        return blocking;
    }

    public void setBlocking(boolean blocking) {
        this.blocking = blocking;
        notifyPropertyChanged(BR.blocking);
    }

    @Bindable
    public boolean isReportingGallery() {
        return reportingGallery;
    }

    public void setReportingGallery(boolean reportingGallery) {
        this.reportingGallery = reportingGallery;
        notifyPropertyChanged(BR.reportingGallery);
    }

    @Bindable
    public boolean isNoOwner() {
        return noOwner;
    }

    public void setNoOwner(boolean noOwner) {
        this.noOwner = noOwner;
        notifyPropertyChanged(BR.noOwner);

    }

    @Bindable
    public boolean isFromComment() {
        return fromComment;
    }

    public void setFromComment(boolean fromComment) {
        this.fromComment = fromComment;
        notifyPropertyChanged(BR.fromComment);
    }

    @Bindable
    public boolean isMyGallery() {
        return myGallery;
    }

    public void setMyGallery(boolean myGallery) {
        this.myGallery = myGallery;
        notifyPropertyChanged(BR.myGallery);
    }

    @Bindable
    public boolean isMyComment() {
        return myComment;
    }

    public void setMyComment(boolean myComment) {
        this.myComment = myComment;
        notifyPropertyChanged(BR.myComment);
    }

    @Bindable
    public boolean isFollowing() {
        return following;
    }

    public void setFollowing(boolean following) {
        this.following = following;
        notifyPropertyChanged(BR.following);
    }

    @Bindable
    public String getReportUserText() {
        return reportUserText;
    }

    public void setReportUserText(String reportUserText) {
        this.reportUserText = reportUserText;
        notifyPropertyChanged(BR.reportUserText);
    }

    @Bindable
    public String getBlockUserText() {
        return blockUserText;
    }

    public void setBlockUserText(String blockUserText) {
        this.blockUserText = blockUserText;
        notifyPropertyChanged(BR.blockUserText);
    }

    @Bindable
    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
        notifyPropertyChanged(BR.blocked);
    }

    public Action1<View> toggleFollow = view -> {
        if (listener != null && view != null) {
            listener.onToggleFollow(isFollowing());
        }
    };

    public Action1<View> reportGallery = view -> {
        if (listener != null && view != null) {
            listener.onReportGallery();
        }
    };

    public Action1<View> reportUser = view -> {
        if (listener != null && view != null) {
            listener.onReportUser();
        }
    };

    public Action1<View> toggleBlockUser = view -> {
        if (listener != null && view != null) {
            listener.onToggleBlockUser(blocking);
        }
    };

    public Action1<View> deleteComment = view -> {
        if (listener != null && view != null) {
            listener.onDeleteComment();
        }
    };

    public interface OnReportAndBlockListener {
        void onReportGallery();

        void onToggleFollow(boolean following);

        void onReportUser();

        void onToggleBlockUser(boolean blocking);

        void onDeleteComment();
    }
}
