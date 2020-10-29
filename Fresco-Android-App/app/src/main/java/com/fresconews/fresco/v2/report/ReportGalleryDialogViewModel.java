package com.fresconews.fresco.v2.report;

import android.app.Activity;
import android.databinding.Bindable;
import android.view.View;
import android.widget.EditText;

import com.fresconews.fresco.BR;
import com.fresconews.fresco.framework.databinding.bindingTypes.BindableView;
import com.fresconews.fresco.framework.mvvm.DialogViewModel;
import com.fresconews.fresco.v2.utils.KeyboardUtils;
import com.fresconews.fresco.v2.utils.LogUtils;

import rx.functions.Action1;

/**
 * Created by wumau on 12/13/2016.
 */
public class ReportGalleryDialogViewModel extends DialogViewModel {

    public BindableView<EditText> detailsEditText = new BindableView<>();

    private View dialogView;
    private String reportTitle;
    private boolean abusiveChecked;
    private boolean spamChecked;
    private boolean stolenContentChecked;
    private boolean graphicContentChecked;

    private ReportGalleryDialogViewModel.OnReportGalleryListener listener;

    public ReportGalleryDialogViewModel(Activity activity, ReportGalleryDialogViewModel.OnReportGalleryListener listener) {
        super(activity);

        this.listener = listener;
    }

    @Bindable
    public String getReportTitle() {
        return reportTitle;
    }

    public void setReportTitle(String reportTitle) {
        this.reportTitle = reportTitle;
        notifyPropertyChanged(BR.reportTitle);
    }

    @Bindable
    public boolean isAbusiveChecked() {
        return abusiveChecked;
    }

    public void setAbusiveChecked(boolean abusiveChecked) {
        this.abusiveChecked = abusiveChecked;
        notifyPropertyChanged(BR.abusiveChecked);
    }

    @Bindable
    public boolean isSpamChecked() {
        return spamChecked;
    }

    public void setSpamChecked(boolean spamChecked) {
        this.spamChecked = spamChecked;
        notifyPropertyChanged(BR.spamChecked);
    }

    @Bindable
    public boolean isStolenContentChecked() {
        return stolenContentChecked;
    }

    public void setStolenContentChecked(boolean stolenContentChecked) {
        this.stolenContentChecked = stolenContentChecked;
        notifyPropertyChanged(BR.stolenContentChecked);
    }

    @Bindable
    public boolean isGraphicContentChecked() {
        return graphicContentChecked;
    }

    public void setGraphicContentChecked(boolean graphicContentChecked) {
        this.graphicContentChecked = graphicContentChecked;
        notifyPropertyChanged(BR.graphicContentChecked);
    }

    public Action1<View> checkAbusive = view -> {
        setSpamChecked(false);
        setStolenContentChecked(false);
        setGraphicContentChecked(false);
        setAbusiveChecked(!isAbusiveChecked());
    };

    public Action1<View> checkSpam = view -> {
        setAbusiveChecked(false);
        setStolenContentChecked(false);
        setGraphicContentChecked(false);
        setSpamChecked(!isSpamChecked());
    };

    public Action1<View> checkStolenContent = view -> {
        setAbusiveChecked(false);
        setSpamChecked(false);
        setGraphicContentChecked(false);
        setStolenContentChecked(!isStolenContentChecked());
    };

    public Action1<View> checkGraphicContent = view -> {
        setAbusiveChecked(false);
        setSpamChecked(false);
        setStolenContentChecked(false);
        setGraphicContentChecked(!isStolenContentChecked());
    };

    public Action1<View> cancel = view -> {
        KeyboardUtils.hideKeyboardFromFragment(activity, view);
        dismiss();
    };

    public Action1<View> sendReport = view -> {
        if (listener != null) {
            String reason = "";
            if (isAbusiveChecked()) {
                reason = "abuse";
            }
            else if (isSpamChecked()) {
                reason = "spam";
            }
            else if (isStolenContentChecked()) {
                reason = "stolen";
            }
            else if (isGraphicContentChecked()) {
                reason = "nsfw";
            }
            if (reason.isEmpty()) {
                // TODO
            }
            else {
                KeyboardUtils.hideKeyboardFromFragment(activity, view);
                listener.sendReport(reason, detailsEditText.get().getText().toString());
                dismiss();
            }
        }
    };

    public interface OnReportGalleryListener {
        void sendReport(String reason, String message);
    }
}
