package com.fresconews.fresco.framework.databinding.bindingTypes;

import com.fresconews.fresco.framework.databinding.bindingUtils.Objects;

public class BindableString extends BaseObservable {

    String value;
    boolean truncated = false;
    // ifNullThenRemove will remove views if null View.GONE
    boolean nullThenRemoveView = false;
    String postId;
    // for linking imageUrl to VideoViews for setting background preview pic
    String loadState;

    public String get() {
        return value != null ? value : "";
    }

    public boolean isTruncated() {
        return truncated;
    }

    public boolean isNullThenRemoveView() {
        return nullThenRemoveView;
    }

    public void set(String value) {
        if (!Objects.equals(this.value, value)) {
            this.value = value;
            notifyChange();
        }
    }

    public void setPostId(String postId) {
        if (!Objects.equals(this.postId, postId)) {
            this.postId = postId;
        }

    }

    public String getPostId() {
        return postId != null ? postId : "";
    }

    public void setLoadState(String loadState) {
        if (!Objects.equals(this.loadState, loadState)) {
            this.loadState = loadState;
        }

    }

    public String getLoadState() {
        return loadState != null ? loadState : "";
    }

    public void setTruncated(boolean truncated) {
        this.truncated = truncated;
    }

    public void setNullThenRemoveView(boolean nullThenRemove) {
        this.nullThenRemoveView = nullThenRemove;
    }

    public boolean isEmpty() {
        return value == null || value.isEmpty();
    }
}