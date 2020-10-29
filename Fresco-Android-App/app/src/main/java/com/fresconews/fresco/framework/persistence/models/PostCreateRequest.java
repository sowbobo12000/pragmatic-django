package com.fresconews.fresco.framework.persistence.models;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.fresconews.fresco.framework.network.requests.NetworkGalleryCreateRequest;
import com.fresconews.fresco.v2.utils.ContentUtils;
import com.fresconews.fresco.v2.utils.LogUtils;

import java.util.Date;

public class PostCreateRequest implements Parcelable {
    private String address;
    private Uri uri;
    private double lat;
    private double lng;
    private Date capturedAt;

    public PostCreateRequest() {
    }

    public PostCreateRequest setAddress(String address) {
        this.address = address;
        return this;
    }

    public PostCreateRequest setUri(Uri uri) {
        this.uri = uri;
        return this;
    }

    public PostCreateRequest setLatitude(double lat) {
        this.lat = lat;
        return this;
    }

    public PostCreateRequest setLongitude(double lng) {
        this.lng = lng;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public Uri getUri() {
        return uri;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public Date getCapturedAt() {
        return capturedAt;
    }

    public PostCreateRequest setCapturedAt(Date capturedAt) {
        LogUtils.i("boomoo", "captured at - " + capturedAt.toString());
        this.capturedAt = capturedAt;
        return this;
    }

    public String getMediaType(Context context) {
        if (uri.getScheme().equals(ContentResolver.SCHEME_FILE)) {
            String filePath = uri.toString();
            if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg")) {
                return "image/jpg";
            }
            else if (filePath.endsWith(".mp4")) {
                return "video/mp4";
            }
        }
        else {
            return context.getContentResolver().getType(uri);
        }

        return "application/octet-stream";
    }

    public boolean isVideoType(Context context) {
        String type = ContentUtils.getMediaType(context, getUri());
        return type != null && type.contains("video");
    }

    public NetworkGalleryCreateRequest.NetworkPostCreateRequest toNetwork(Context context) {
        NetworkGalleryCreateRequest.NetworkPostCreateRequest request = new NetworkGalleryCreateRequest.NetworkPostCreateRequest();

        request.setAddress(address);
        request.setLat(lat);
        request.setLng(lng);
        request.setCapturedAt(capturedAt);
        LogUtils.i("PostCreate", "captured at - " + capturedAt.toString());

        request.setContentType(getMediaType(context));
        request.setChunkSize(request.getFileSize());

        return request;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    //<editor-fold desc="Parcelable">
    protected PostCreateRequest(Parcel in) {
        address = in.readString();
        uri = in.readParcelable(Uri.class.getClassLoader());
        lat = in.readDouble();
        lng = in.readDouble();
        capturedAt = new Date(in.readLong());
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(address);
        parcel.writeParcelable(uri, flags);
        parcel.writeDouble(lat);
        parcel.writeDouble(lng);
        parcel.writeLong(capturedAt.getTime());
    }

    public static final Creator<PostCreateRequest> CREATOR = new Creator<PostCreateRequest>() {
        @Override
        public PostCreateRequest createFromParcel(Parcel in) {
            return new PostCreateRequest(in);
        }

        @Override
        public PostCreateRequest[] newArray(int size) {
            return new PostCreateRequest[size];
        }
    };
    //</editor-fold>
}
