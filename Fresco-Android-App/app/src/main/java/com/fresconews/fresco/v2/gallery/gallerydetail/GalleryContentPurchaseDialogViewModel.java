package com.fresconews.fresco.v2.gallery.gallerydetail;

import android.app.Activity;
import android.app.Dialog;
import android.databinding.Bindable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.fresconews.fresco.BR;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.databinding.bindingTypes.BindableView;
import com.fresconews.fresco.framework.mvvm.DialogViewModel;
import com.fresconews.fresco.framework.mvvm.datasources.ListDataSource;
import com.fresconews.fresco.framework.mvvm.datasources.PostViewModelDataSource;
import com.fresconews.fresco.framework.mvvm.viewmodels.PostViewModel;
import com.fresconews.fresco.framework.persistence.models.Post;
import com.fresconews.fresco.framework.recyclerview.RecyclerViewBindingAdapter;

import java.util.List;

import io.smooch.ui.ConversationActivity;
import rx.functions.Action1;

import static com.fresconews.fresco.v2.gallery.gallerydetail.GalleryDetailStatusViewModel.STATUS_DELETED;
import static com.fresconews.fresco.v2.gallery.gallerydetail.GalleryDetailStatusViewModel.STATUS_HIGHLIGHTED;
import static com.fresconews.fresco.v2.gallery.gallerydetail.GalleryDetailStatusViewModel.STATUS_NOT_VERIFIED;
import static com.fresconews.fresco.v2.gallery.gallerydetail.GalleryDetailStatusViewModel.STATUS_PENDING_VERIFICATION;
import static com.fresconews.fresco.v2.gallery.gallerydetail.GalleryDetailStatusViewModel.STATUS_SOLD;
import static com.fresconews.fresco.v2.gallery.gallerydetail.GalleryDetailStatusViewModel.STATUS_VERIFIED;

/**
 * Created by wumau on 12/12/2016.
 */
public class GalleryContentPurchaseDialogViewModel extends DialogViewModel {

    public BindableView<RecyclerView> recyclerView = new BindableView<>();

    private List<Post> postsPurchased;

    private int status;
    private String firstLineTitle;
    private String secondLineTitle;
    private String thirdLineTitle;
    private String fourthLineTitle;
    private String secondLineMessage;
    private String fourthLineMessage;
    private Drawable firstLineIcon;
    private Drawable secondLineIcon;
    private Drawable thirdLineIcon;
    private Drawable fourthLineIcon;
    private int firstToSecondLineColor;
    private int secondToThirdLineColor;
    private int thirdToFourthLineColor;
    private boolean secondLineBold;
    private boolean thirdLineBold;

    public GalleryContentPurchaseDialogViewModel(Activity activity, int status, boolean anonymous, List<Post> postsPurchased) {
        super(activity);

        this.postsPurchased = postsPurchased;
        setUI(status, anonymous);
    }

    @Override
    public void onBound() {
        if (hasBeenBound) {
            return;
        }
        super.onBound();

        if (status == STATUS_SOLD || status == STATUS_DELETED) {
            PostViewModelDataSource dataSource = new PostViewModelDataSource(activity, new ListDataSource<>(postsPurchased));
            RecyclerViewBindingAdapter<PostViewModel> adapter = new RecyclerViewBindingAdapter<>(R.layout.item_purchased_post, dataSource);
            recyclerView.get().setLayoutManager(new LinearLayoutManager(activity));
            recyclerView.get().setAdapter(adapter);
        }
    }

    private void setUI(int status, boolean anonymous) {
        this.status = status;
        setFirstLineTitle(anonymous ? getString(R.string.submitted_anonymously) : getString(R.string.submitted));
        switch (status) {
            case STATUS_PENDING_VERIFICATION:
                setSecondLineTitle(getString(R.string.pending_verification));
                setSecondLineMessage(getString(R.string.pending_message));
                setSecondLineBold(true);
                setFirstLineIcon(ContextCompat.getDrawable(activity, R.drawable.ic_checkbox_marked_circle_outline_green));
                setSecondLineIcon(ContextCompat.getDrawable(activity, R.drawable.ic_checkbox_blank_circle_outline_yellow));
                setFirstToSecondLineColor(ContextCompat.getColor(activity, R.color.fresco_yellow));
                break;
            case STATUS_NOT_VERIFIED:
                setSecondLineTitle(getString(R.string.couldnt_verify));
                setSecondLineMessage(getString(R.string.couldnt_verify_message));
                setSecondLineBold(true);
                setFirstLineIcon(ContextCompat.getDrawable(activity, R.drawable.ic_checkbox_marked_circle_outline_green));
                setSecondLineIcon(ContextCompat.getDrawable(activity, R.drawable.ic_close_circle_outline_26));
                setFirstToSecondLineColor(ContextCompat.getColor(activity, R.color.black_26));
                break;
            case STATUS_VERIFIED:
                setSecondLineTitle(getString(R.string.verified));
                setSecondLineMessage(getString(R.string.verified_message));
                setSecondLineBold(true);
                setFirstLineIcon(ContextCompat.getDrawable(activity, R.drawable.ic_checkbox_marked_circle_outline_green));
                setSecondLineIcon(ContextCompat.getDrawable(activity, R.drawable.ic_checkbox_marked_circle_outline_green));
                setFirstToSecondLineColor(ContextCompat.getColor(activity, R.color.fresco_green));
                break;
            case STATUS_HIGHLIGHTED:
                setSecondLineTitle(getString(R.string.verified));
                setSecondLineMessage(getString(R.string.verified_message));
                setSecondLineBold(true);
                setFirstLineIcon(ContextCompat.getDrawable(activity, R.drawable.ic_checkbox_marked_circle_outline_green));
                setSecondLineIcon(ContextCompat.getDrawable(activity, R.drawable.ic_checkbox_marked_circle_outline_green));
                setFirstToSecondLineColor(ContextCompat.getColor(activity, R.color.fresco_green));
                break;
            case STATUS_SOLD:
                setSecondLineTitle(getString(R.string.verified));
                setSecondLineBold(false);
                setThirdLineBold(true);
                setFirstLineIcon(ContextCompat.getDrawable(activity, R.drawable.ic_checkbox_marked_circle_outline_green));
                setSecondLineIcon(ContextCompat.getDrawable(activity, R.drawable.ic_checkbox_marked_circle_outline_green));
                setFirstToSecondLineColor(ContextCompat.getColor(activity, R.color.fresco_green));
                setThirdLineTitle(getString(R.string.sold));
                setThirdLineIcon(ContextCompat.getDrawable(activity, R.drawable.ic_cash_usd_green));
                setSecondToThirdLineColor(ContextCompat.getColor(activity, R.color.fresco_green));
                break;
            case STATUS_DELETED:
                setSecondLineTitle(getString(R.string.verified));
                setSecondLineBold(false);
                setThirdLineBold(true);
                setFirstLineIcon(ContextCompat.getDrawable(activity, R.drawable.ic_checkbox_marked_circle_outline_26));
                setSecondLineIcon(ContextCompat.getDrawable(activity, R.drawable.ic_checkbox_marked_circle_outline_26));
                setFirstToSecondLineColor(ContextCompat.getColor(activity, R.color.black_26));
                setThirdLineTitle(getString(R.string.sold));
                setThirdLineIcon(ContextCompat.getDrawable(activity, R.drawable.ic_cash_usd_26));
                setSecondToThirdLineColor(ContextCompat.getColor(activity, R.color.black_26));
                setFourthLineTitle(getString(R.string.deleted));
                setFourthLineMessage(getString(R.string.deleted_message));
                setFourthLineIcon(ContextCompat.getDrawable(activity, R.drawable.ic_close_circle_outline_red));
                setThirdToFourthLineColor(ContextCompat.getColor(activity, R.color.fresco_red));
                break;
        }
    }

    @Bindable
    public int getStatus() {
        return status;
    }

    @Bindable
    public String getFirstLineTitle() {
        return firstLineTitle;
    }

    public void setFirstLineTitle(String firstLineTitle) {
        this.firstLineTitle = firstLineTitle;
        notifyPropertyChanged(BR.firstLineTitle);
    }

    @Bindable
    public String getSecondLineTitle() {
        return secondLineTitle;
    }

    public void setSecondLineTitle(String secondLineTitle) {
        this.secondLineTitle = secondLineTitle;
        notifyPropertyChanged(BR.secondLineTitle);
    }

    @Bindable
    public String getThirdLineTitle() {
        return thirdLineTitle;
    }

    public void setThirdLineTitle(String thirdLineTitle) {
        this.thirdLineTitle = thirdLineTitle;
        notifyPropertyChanged(BR.thirdLineTitle);
    }

    @Bindable
    public String getSecondLineMessage() {
        return secondLineMessage;
    }

    public void setSecondLineMessage(String secondLineMessage) {
        this.secondLineMessage = secondLineMessage;
        notifyPropertyChanged(BR.secondLineMessage);
    }

    @Bindable
    public Drawable getFirstLineIcon() {
        return firstLineIcon;
    }

    public void setFirstLineIcon(Drawable firstLineIcon) {
        this.firstLineIcon = firstLineIcon;
        notifyPropertyChanged(BR.firstLineIcon);
    }

    @Bindable
    public Drawable getSecondLineIcon() {
        return secondLineIcon;
    }

    public void setSecondLineIcon(Drawable secondLineIcon) {
        this.secondLineIcon = secondLineIcon;
        notifyPropertyChanged(BR.secondLineIcon);
    }

    @Bindable
    public Drawable getThirdLineIcon() {
        return thirdLineIcon;
    }

    public void setThirdLineIcon(Drawable thirdLineIcon) {
        this.thirdLineIcon = thirdLineIcon;
        notifyPropertyChanged(BR.thirdLineIcon);
    }

    @Bindable
    public int getFirstToSecondLineColor() {
        return firstToSecondLineColor;
    }

    public void setFirstToSecondLineColor(int firstToSecondLineColor) {
        this.firstToSecondLineColor = firstToSecondLineColor;
        notifyPropertyChanged(BR.firstToSecondLineColor);
    }

    @Bindable
    public int getSecondToThirdLineColor() {
        return secondToThirdLineColor;
    }

    public void setSecondToThirdLineColor(int secondToThirdLineColor) {
        this.secondToThirdLineColor = secondToThirdLineColor;
        notifyPropertyChanged(BR.secondToThirdLineColor);
    }

    @Bindable
    public String getFourthLineTitle() {
        return fourthLineTitle;
    }

    public void setFourthLineTitle(String fourthLineTitle) {
        this.fourthLineTitle = fourthLineTitle;
        notifyPropertyChanged(BR.fourthLineTitle);
    }

    @Bindable
    public Drawable getFourthLineIcon() {
        return fourthLineIcon;
    }

    public void setFourthLineIcon(Drawable fourthLineIcon) {
        this.fourthLineIcon = fourthLineIcon;
        notifyPropertyChanged(BR.fourthLineIcon);
    }

    @Bindable
    public int getThirdToFourthLineColor() {
        return thirdToFourthLineColor;
    }

    public void setThirdToFourthLineColor(int thirdToFourthLineColor) {
        this.thirdToFourthLineColor = thirdToFourthLineColor;
        notifyPropertyChanged(BR.thirdToFourthLineColor);
    }

    @Bindable
    public String getFourthLineMessage() {
        return fourthLineMessage;
    }

    public void setFourthLineMessage(String fourthLineMessage) {
        this.fourthLineMessage = fourthLineMessage;
        notifyPropertyChanged(BR.fourthLineMessage);
    }

    @Bindable
    public boolean isThirdLineBold() {
        return thirdLineBold;
    }

    public void setThirdLineBold(boolean thirdLineBold) {
        this.thirdLineBold = thirdLineBold;
        notifyPropertyChanged(BR.thirdLineBold);
    }

    @Bindable
    public boolean isSecondLineBold() {
        return secondLineBold;
    }

    public void setSecondLineBold(boolean secondLineBold) {
        this.secondLineBold = secondLineBold;
        notifyPropertyChanged(BR.secondLineBold);
    }

    public Action1<View> getHelp = view -> {
        dismiss();
        ConversationActivity.show(activity);
    };

    public Action1<View> close = view -> {
        dismiss();
    };

    @Override
    public Dialog getDialog() {
        return super.getDialog();
    }
}
