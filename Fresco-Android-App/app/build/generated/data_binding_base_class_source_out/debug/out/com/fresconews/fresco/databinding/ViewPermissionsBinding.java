// Generated by data binding compiler. Do not edit!
package com.fresconews.fresco.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.Bindable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import com.fresconews.fresco.R;
import com.fresconews.fresco.v2.login.TOSDialogViewModel;
import java.lang.Deprecated;
import java.lang.Object;

public abstract class ViewPermissionsBinding extends ViewDataBinding {
  @Bindable
  protected TOSDialogViewModel mModel;

  protected ViewPermissionsBinding(Object _bindingComponent, View _root, int _localFieldCount) {
    super(_bindingComponent, _root, _localFieldCount);
  }

  public abstract void setModel(@Nullable TOSDialogViewModel model);

  @Nullable
  public TOSDialogViewModel getModel() {
    return mModel;
  }

  @NonNull
  public static ViewPermissionsBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup root, boolean attachToRoot) {
    return inflate(inflater, root, attachToRoot, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.inflate(inflater, R.layout.view_permissions, root, attachToRoot, component)
   */
  @NonNull
  @Deprecated
  public static ViewPermissionsBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup root, boolean attachToRoot, @Nullable Object component) {
    return ViewDataBinding.<ViewPermissionsBinding>inflateInternal(inflater, R.layout.view_permissions, root, attachToRoot, component);
  }

  @NonNull
  public static ViewPermissionsBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.inflate(inflater, R.layout.view_permissions, null, false, component)
   */
  @NonNull
  @Deprecated
  public static ViewPermissionsBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable Object component) {
    return ViewDataBinding.<ViewPermissionsBinding>inflateInternal(inflater, R.layout.view_permissions, null, false, component);
  }

  public static ViewPermissionsBinding bind(@NonNull View view) {
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
  public static ViewPermissionsBinding bind(@NonNull View view, @Nullable Object component) {
    return (ViewPermissionsBinding)bind(component, view, R.layout.view_permissions);
  }
}
