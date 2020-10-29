package com.fresconews.fresco.framework.persistence.models;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.fresconews.fresco.framework.network.requests.NetworkGalleryCreateRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ryan on 7/5/2016.
 */
public class GalleryCreateRequest implements Parcelable {
    private String caption;
    private String assignmentId;
    private String outletId;
    private List<PostCreateRequest> posts;

    public GalleryCreateRequest() {
        posts = new ArrayList<>();
    }

    public GalleryCreateRequest setCaption(String caption) {
        this.caption = caption;
        return this;
    }

    public GalleryCreateRequest setAssignment(String id) {
        this.assignmentId = id;
        return this;
    }

    public GalleryCreateRequest setOutletId(String id) {
        this.outletId = id;
        return this;
    }

    public GalleryCreateRequest addPost(PostCreateRequest request) {
        posts.add(request);
        return this;
    }

    public String getCaption() {
        return caption;
    }

    public String getAssignmentId() {
        return assignmentId;
    }

    public String getOutletId() {
        return outletId;
    }

    public List<PostCreateRequest> getPosts() {
        return posts;
    }

    public NetworkGalleryCreateRequest toNetwork(Context context) {
        NetworkGalleryCreateRequest request = new NetworkGalleryCreateRequest();

        request.setCaption(caption);
        request.setAssignmentId(assignmentId);
        request.setOutletId(outletId);

        NetworkGalleryCreateRequest.NetworkPostCreateRequest[] networkPosts = new NetworkGalleryCreateRequest.NetworkPostCreateRequest[posts.size()];
        for (int i = 0; i < networkPosts.length; i++) {
            networkPosts[i] = posts.get(i).toNetwork(context);
        }
        request.setPosts(networkPosts);

        return request;
    }

    //<editor-fold desc="Parcelable">
    protected GalleryCreateRequest(Parcel in) {
        caption = in.readString();
        assignmentId = in.readString();
        outletId = in.readString();
        posts = in.createTypedArrayList(PostCreateRequest.CREATOR);
    }

    public static final Creator<GalleryCreateRequest> CREATOR = new Creator<GalleryCreateRequest>() {
        @Override
        public GalleryCreateRequest createFromParcel(Parcel in) {
            return new GalleryCreateRequest(in);
        }

        @Override
        public GalleryCreateRequest[] newArray(int size) {
            return new GalleryCreateRequest[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(caption);
        parcel.writeString(assignmentId);
        parcel.writeString(outletId);
        parcel.writeTypedList(posts);
    }
    //</editor-fold>
}
