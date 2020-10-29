package com.fresconews.fresco.framework.mvvm.viewmodels;

import android.app.Activity;
import android.databinding.Bindable;
import android.view.View;

import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.mvvm.ActivityViewModel;
import com.fresconews.fresco.framework.mvvm.ItemViewModel;
import com.fresconews.fresco.framework.persistence.models.Assignment;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.Seconds;

import java.lang.ref.WeakReference;

import rx.functions.Action1;

public class AssignmentViewModel extends ItemViewModel<Assignment> {

    private WeakReference<ActivityViewModel> mActivityViewModel;

    public AssignmentViewModel(ActivityViewModel activityViewModel, Assignment item) {
        super(item);
        mActivityViewModel = new WeakReference<>(activityViewModel);
    }

    @Bindable
    public String getTitle() {
        return getItem().getTitle();
    }

    @Bindable
    public String getCaption() {
        return getItem().getCaption();
    }

    @Bindable
    public String getExpirationString() {
        if (getItem() == null) {
            return "";
        }

        Activity activity = mActivityViewModel.get().getActivity();
        int days = Days.daysBetween(new DateTime(), new DateTime(getItem().getEndsAt())).getDays();
        int hours = Hours.hoursBetween(new DateTime(), new DateTime(getItem().getEndsAt())).getHours();
        int minutes = Minutes.minutesBetween(new DateTime(), new DateTime(getItem().getEndsAt())).getMinutes();
        int seconds = Seconds.secondsBetween(new DateTime(), new DateTime(getItem().getEndsAt())).getSeconds();

        if (days > 0) {
            return activity.getString(R.string.expires_one_param, activity.getResources().getQuantityString(R.plurals.days, days, days));
        }
        if (hours > 0) {
            if (minutes > 0) {
                return activity.getString(R.string.expires_one_param, activity.getResources().getQuantityString(R.plurals.hours, hours, hours));
            }
            else {
                return activity.getString(R.string.expires_two_param, activity.getResources().getQuantityString(R.plurals.hours, hours, hours),
                        activity.getResources().getQuantityString(R.plurals.minutes, minutes, minutes));
            }
        }
        if (minutes > 0) {
            return activity.getString(R.string.expires_one_param, activity.getResources().getQuantityString(R.plurals.minutes, minutes, minutes));
        }
        if (seconds > 0) {
            return activity.getString(R.string.expires_one_param, activity.getResources().getQuantityString(R.plurals.seconds, seconds, seconds));
        }
        return activity.getString(R.string.error_assignment_expired);
    }

    @SuppressWarnings("unchecked")
    public Action1<View> openCamera = view -> {
        mActivityViewModel.get().openCamera.call(view);
    };
}
