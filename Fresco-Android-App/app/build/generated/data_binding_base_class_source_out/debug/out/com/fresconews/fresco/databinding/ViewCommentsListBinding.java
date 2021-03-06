// Generated by data binding compiler. Do not edit!
package com.fresconews.fresco.databinding;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.Bindable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import com.fresconews.fresco.R;
import com.fresconews.fresco.v2.gallery.gallerydetail.GalleryDetailCommentsViewModel;
import java.lang.Deprecated;
import java.lang.Object;

public abstract class ViewCommentsListBinding extends ViewDataBinding {
  @NonNull
  public final RecyclerView commentsRecyclerView;

  @NonNull
  public final TextView commentsTitleTextview;

  @Bindable
  protected GalleryDetailCommentsViewModel mModel;

  protected ViewCommentsListBinding(Object _bindingComponent, View _root, int _localFieldCount,
      RecyclerView commentsRecyclerView, TextView commentsTitleTextview) {
    super(_bindingComponent, _root, _localFieldCount);
    this.commentsRecyclerView = commentsRecyclerView;
    this.commentsTitleTextview = commentsTitleTextview;
  }

  public abstract void setModel(@Nullable GalleryDetailCommentsViewModel model);

  @Nullable
  public GalleryDetailCommentsViewModel getModel() {
    return mModel;
  }

  @NonNull
  public static ViewCommentsListBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup root, boolean attachToRoot) {
    return inflate(inflater, root, attachToRoot, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.inflate(inflater, R.layout.view_comments_list, root, attachToRoot, component)
   */
  @NonNull
  @Deprecated
  public static ViewCommentsListBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup root, boolean attachToRoot, @Nullable Object component) {
    return ViewDataBinding.<ViewCommentsListBinding>inflateInternal(inflater, R.layout.view_comments_list, root, attachToRoot, component);
  }

  @NonNull
  public static ViewCommentsListBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.inflate(inflater, R.layout.view_comments_list, null, false, component)
   */
  @NonNull
  @Deprecated
  public static ViewCommentsListBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable Object component) {
    return ViewDataBinding.<ViewCommentsListBinding>inflateInternal(inflater, R.layout.view_comments_list, null, false, component);
  }

  public static ViewCommentsListBinding bind(@NonNull View view) {
    return bind(view, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.bind(view, component)
   */
  @Deprecated
  public static ViewCommentsListBinding bind(@NonNull View view, @Nullable Object component) {
    return (ViewCommentsListBinding)bind(component, view, R.layout.view_comments_list);
  }
}
