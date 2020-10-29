package com.fresconews.fresco.v2.gallery.gallerydetail;

import android.databinding.Bindable;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.fresconews.fresco.BR;
import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.base.BaseActivity;
import com.fresconews.fresco.framework.mvvm.ViewModel;
import com.fresconews.fresco.framework.persistence.managers.GalleryManager;
import com.fresconews.fresco.framework.persistence.models.Post;
import com.fresconews.fresco.framework.persistence.models.Purchase;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by wumau on 12/9/2016.
 */
public class GalleryDetailStatusViewModel extends ViewModel {

    public static final int STATUS_NONE = -1;
    public static final int STATUS_PENDING_VERIFICATION = 0;
    public static final int STATUS_NOT_VERIFIED = 1;
    public static final int STATUS_VERIFIED = 2;
    public static final int STATUS_HIGHLIGHTED = 3;
    public static final int STATUS_DELETED = 4;
    public static final int STATUS_SOLD = 17;

    @Inject
    GalleryManager galleryManager;

    private GalleryContentPurchaseDialogViewModel purchaseDialogVieModel;
    private BaseActivity activity;

    private List<Post> postsPurchased;
    private boolean hasBeenBound;
    private boolean anonymous;
    private int backgroundColor;
    private String statusText;
    private int status;

    public GalleryDetailStatusViewModel(GalleryDetailActivity activity) {
        ((Fresco2) activity.getApplication()).getFrescoComponent().inject(this);

        this.activity = activity;

        status = STATUS_NONE;
        anonymous = false;
    }

    public void onBound() {
        if (hasBeenBound) {
            return;
        }
        hasBeenBound = true;
    }

    private void setUI() {
        switch (status) {
            case STATUS_PENDING_VERIFICATION:
                setBackgroundColor(ContextCompat.getColor(activity, R.color.fresco_yellow));
                setStatusText(activity.getString(R.string.pending_verification));
                break;
            case STATUS_NOT_VERIFIED:
                setBackgroundColor(ContextCompat.getColor(activity, R.color.black_26));
                setStatusText(activity.getString(R.string.not_verified));
                break;
            case STATUS_VERIFIED:
                setBackgroundColor(ContextCompat.getColor(activity, R.color.fresco_green));
                setStatusText(activity.getString(R.string.verified));
                break;
            case STATUS_HIGHLIGHTED:
                setBackgroundColor(ContextCompat.getColor(activity, R.color.fresco_green));
                setStatusText(activity.getString(R.string.verified));
                break;
            case STATUS_SOLD:
                setBackgroundColor(ContextCompat.getColor(activity, R.color.fresco_green));
                break;
            case STATUS_DELETED:
                setBackgroundColor(ContextCompat.getColor(activity, R.color.fresco_red));
                setStatusText(activity.getString(R.string.deleted));
                break;
        }
    }

    public void loadPurchases(String galleryId) {
        galleryManager.getPurchases(galleryId)
                      .subscribeOn(Schedulers.io())
                      .observeOn(AndroidSchedulers.mainThread())
                      .onErrorReturn(throwable -> null)
                      .subscribe(posts -> {
                          if (posts != null) {
                              postsPurchased = new ArrayList<>();
                              List<String> outletsNames = new ArrayList<>();
                              for (Post post : posts) {
                                  boolean purchased = false;
                                  for (Purchase purchase : post.loadPurchases()) {
                                      if (purchase.getOutlet() != null && !outletsNames.contains(purchase.getOutlet().getTitle())) {
                                          outletsNames.add(purchase.getOutlet().getTitle());
                                      }
                                      purchased = true;
                                  }
                                  if (purchased) {
                                      postsPurchased.add(post);
                                  }
                              }
                              if (outletsNames.size() > 1) {
                                  setStatusText(activity.getString(R.string.sold_to_outlets, outletsNames.size()));
                                  setStatus(STATUS_SOLD);
                              }
                              else if (outletsNames.size() == 1) {
                                  setStatusText(activity.getString(R.string.sold_to, outletsNames.get(0)));
                                  setStatus(STATUS_SOLD);
                              }
                          }
                      });
    }

    public Action1<View> viewStatus = view -> {
        if (purchaseDialogVieModel == null) {
            purchaseDialogVieModel = new GalleryContentPurchaseDialogViewModel(activity, status, anonymous, postsPurchased);
        }

        if (!purchaseDialogVieModel.isShowing()) {
            purchaseDialogVieModel.show(R.layout.view_gallery_status_details);
            purchaseDialogVieModel.getDialog().setCancelable(true); //dialog isn't instantiated until DialogViewModel calls show()
        }
    };

    @Bindable
    public int getStatus() {
        return status;
    }

    public void setStatus(int rating) {
        this.status = rating;
        notifyPropertyChanged(BR.status);
        setUI();
    }

    @Bindable
    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        notifyPropertyChanged(BR.backgroundColor);
    }

    @Bindable
    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
        notifyPropertyChanged(BR.statusText);
    }

    @Bindable
    public boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
        notifyPropertyChanged(BR.anonymous);
    }
}
