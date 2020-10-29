package com.fresconews.fresco.v2.mediabrowser;

import android.content.Intent;
import android.databinding.Bindable;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.fresconews.fresco.BR;
import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.databinding.bindingTypes.BindableView;
import com.fresconews.fresco.framework.mvvm.ViewModel;
import com.fresconews.fresco.v2.utils.LogUtils;
import com.fresconews.fresco.v2.views.FrescoSubmissionVideoView;

import java.util.Date;
import java.util.Locale;

import rx.functions.Action1;
import rx.functions.Action2;

public class MediaItemViewModel extends ViewModel implements Parcelable {
    private Uri uri;
    private double lat;
    private double lng;
    private String mimeType;
    private Date date;
    private long duration;
    private boolean selected;
    private long capturedAt;

    @Bindable
    public int width = 1024;

    @Bindable
    public int height = 1024;

    public BindableView<ImageView> imageView = new BindableView<>();
    public BindableView<FrescoSubmissionVideoView> videoView = new BindableView<>();
    public BindableView<TextView> bindableTV = new BindableView<>();


    public Action2<MediaItemViewModel, Boolean> onClicked;

    public MediaItemViewModel() {
    }

    @Override
    public void onBound() {
        super.onBound();
        setClickableSpan();
    }

    public Action1<View> selectToggle = view -> {
        if (mimeType.contains("video") && duration >= 61000) {
            if (onClicked != null) {
                onClicked.call(this, true);
            }
            return;
        }

        setSelected(!selected);
        if (selected) {
            imageView.get().setColorFilter(Color.parseColor("#46FFFFFF"), PorterDuff.Mode.SCREEN);
        }
        else {
            imageView.get().clearColorFilter();
        }

        if (onClicked != null) {
            onClicked.call(this, false);
        }
    };

    @Bindable
    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    @Bindable
    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
        notifyPropertyChanged(BR.lat);
    }

    @Bindable
    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
        notifyPropertyChanged(BR.lng);
    }

    @Bindable
    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
        notifyPropertyChanged(BR.mimeType);
    }

    @Bindable
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
        notifyPropertyChanged(BR.date);
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
        notifyPropertyChanged(BR.durationString);
    }

    @Bindable
    public String getDurationString() {
        if (duration == 0) {
            return "";
        }
        return getHumanReadableDuration(duration);
    }

    @Bindable
    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        notifyPropertyChanged(BR.selected);
    }

    public static String getHumanReadableDuration(long duration) {
        long durationSeconds = duration / 1000;
        long seconds = durationSeconds % 60;
        long minutes = durationSeconds / 60;

        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
    }

    //<editor-fold desc="Parcelable">
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(uri, i);
        parcel.writeDouble(lat);
        parcel.writeDouble(lng);
        parcel.writeString(mimeType);
        parcel.writeLong(duration);
        parcel.writeByte((byte) (selected ? 1 : 0));
        parcel.writeLong(date.getTime());

    }

    protected MediaItemViewModel(Parcel in) {
        uri = in.readParcelable(Uri.class.getClassLoader());
        lat = in.readDouble();
        lng = in.readDouble();
        mimeType = in.readString();
        duration = in.readLong();
        selected = in.readByte() != 0;
        date = new Date(in.readLong());
    }

    public static final Creator<MediaItemViewModel> CREATOR = new Creator<MediaItemViewModel>() {
        @Override
        public MediaItemViewModel createFromParcel(Parcel in) {
            return new MediaItemViewModel(in);
        }

        @Override
        public MediaItemViewModel[] newArray(int size) {
            return new MediaItemViewModel[size];
        }
    };
    //</editor-fold>


    private void setClickableSpan() {
        if(bindableTV.get() == null){
            return;
        }
        String string = Fresco2.getContext().getResources().getString(R.string.missing_content);
        SpannableString ss = new SpannableString(string);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                Intent viewIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                Fresco2.getContext().startActivity(viewIntent);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(ContextCompat.getColor(Fresco2.getContext(), R.color.black_87));
                ds.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                ds.setUnderlineText(false);
            }
        };
        ss.setSpan(clickableSpan, 112, 139, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        bindableTV.get().setText(ss);
        bindableTV.get().setMovementMethod(LinkMovementMethod.getInstance());
    }
}
