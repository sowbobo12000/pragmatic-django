// Generated by data binding compiler. Do not edit!
package com.fresconews.fresco.databinding;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.Bindable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import com.fresconews.fresco.R;
import com.fresconews.fresco.v2.gallery.gallerylist.GalleryListViewModel;
import java.lang.Deprecated;
import java.lang.Object;

public abstract class ActivityGalleryListBinding extends ViewDataBinding {
  @NonNull
  public final RecyclerView contentList;

  @NonNull
  public final Toolbar errorToolbar;

  @NonNull
  public final FloatingActionButton fab;

  @NonNull
  public final Toolbar toolbar;

  @Bindable
  protected GalleryListViewModel mModel;

  protected ActivityGalleryListBinding(Object _bindingComponent, View _root, int _localFieldCount,
      RecyclerView contentList, Toolbar errorToolbar, FloatingActionButton fab, Toolbar toolbar) {
    super(_bindingComponent, _root, _localFieldCount);
    this.contentList = contentList;
    this.errorToolbar = errorToolbar;
    this.fab = fab;
    this.toolbar = toolbar;
  }

  public abstract void setModel(@Nullable GalleryListViewModel model);

  @Nullable
  public GalleryListViewModel getModel() {
    return mModel;
  }

  @NonNull
  public static ActivityGalleryListBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup root, boolean attachToRoot) {
    return inflate(inflater, root, attachToRoot, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.inflate(inflater, R.layout.activity_gallery_list, root, attachToRoot, component)
   */
  @NonNull
  @Deprecated
  public static ActivityGalleryListBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup root, boolean attachToRoot, @Nullable Object component) {
    return ViewDataBinding.<ActivityGalleryListBinding>inflateInternal(inflater, R.layout.activity_gallery_list, root, attachToRoot, component);
  }

  @NonNull
  public static ActivityGalleryListBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.inflate(inflater, R.layout.activity_gallery_list, null, false, component)
   */
  @NonNull
  @Deprecated
  public static ActivityGalleryListBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable Object component) {
    return ViewDataBinding.<ActivityGalleryListBinding>inflateInternal(inflater, R.layout.activity_gallery_list, null, false, component);
  }

  public static ActivityGalleryListBinding bind(@NonNull View view) {
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
  public static ActivityGalleryListBinding bind(@NonNull View view, @Nullable Object component) {
    return (ActivityGalleryListBinding)bind(component, view, R.layout.activity_gallery_list);
  }
}
