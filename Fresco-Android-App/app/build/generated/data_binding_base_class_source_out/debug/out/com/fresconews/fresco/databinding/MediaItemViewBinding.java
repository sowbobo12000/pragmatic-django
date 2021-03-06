// Generated by data binding compiler. Do not edit!
package com.fresconews.fresco.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.Bindable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import com.fresconews.fresco.R;
import com.fresconews.fresco.v2.mediabrowser.MediaItemViewModel;
import java.lang.Deprecated;
import java.lang.Object;

public abstract class MediaItemViewBinding extends ViewDataBinding {
  @NonNull
  public final CheckBox chkSelected;

  @Bindable
  protected MediaItemViewModel mModel;

  protected MediaItemViewBinding(Object _bindingComponent, View _root, int _localFieldCount,
      CheckBox chkSelected) {
    super(_bindingComponent, _root, _localFieldCount);
    this.chkSelected = chkSelected;
  }

  public abstract void setModel(@Nullable MediaItemViewModel model);

  @Nullable
  public MediaItemViewModel getModel() {
    return mModel;
  }

  @NonNull
  public static MediaItemViewBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup root, boolean attachToRoot) {
    return inflate(inflater, root, attachToRoot, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.inflate(inflater, R.layout.media_item_view, root, attachToRoot, component)
   */
  @NonNull
  @Deprecated
  public static MediaItemViewBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup root, boolean attachToRoot, @Nullable Object component) {
    return ViewDataBinding.<MediaItemViewBinding>inflateInternal(inflater, R.layout.media_item_view, root, attachToRoot, component);
  }

  @NonNull
  public static MediaItemViewBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.inflate(inflater, R.layout.media_item_view, null, false, component)
   */
  @NonNull
  @Deprecated
  public static MediaItemViewBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable Object component) {
    return ViewDataBinding.<MediaItemViewBinding>inflateInternal(inflater, R.layout.media_item_view, null, false, component);
  }

  public static MediaItemViewBinding bind(@NonNull View view) {
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
  public static MediaItemViewBinding bind(@NonNull View view, @Nullable Object component) {
    return (MediaItemViewBinding)bind(component, view, R.layout.media_item_view);
  }
}
