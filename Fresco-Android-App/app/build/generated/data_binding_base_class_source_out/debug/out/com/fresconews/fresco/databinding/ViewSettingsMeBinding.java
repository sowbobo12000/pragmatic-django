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
import com.fresconews.fresco.v2.settings.SettingsMeViewModel;
import java.lang.Deprecated;
import java.lang.Object;

public abstract class ViewSettingsMeBinding extends ViewDataBinding {
  @Bindable
  protected SettingsMeViewModel mModel;

  protected ViewSettingsMeBinding(Object _bindingComponent, View _root, int _localFieldCount) {
    super(_bindingComponent, _root, _localFieldCount);
  }

  public abstract void setModel(@Nullable SettingsMeViewModel model);

  @Nullable
  public SettingsMeViewModel getModel() {
    return mModel;
  }

  @NonNull
  public static ViewSettingsMeBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup root, boolean attachToRoot) {
    return inflate(inflater, root, attachToRoot, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.inflate(inflater, R.layout.view_settings_me, root, attachToRoot, component)
   */
  @NonNull
  @Deprecated
  public static ViewSettingsMeBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup root, boolean attachToRoot, @Nullable Object component) {
    return ViewDataBinding.<ViewSettingsMeBinding>inflateInternal(inflater, R.layout.view_settings_me, root, attachToRoot, component);
  }

  @NonNull
  public static ViewSettingsMeBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.inflate(inflater, R.layout.view_settings_me, null, false, component)
   */
  @NonNull
  @Deprecated
  public static ViewSettingsMeBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable Object component) {
    return ViewDataBinding.<ViewSettingsMeBinding>inflateInternal(inflater, R.layout.view_settings_me, null, false, component);
  }

  public static ViewSettingsMeBinding bind(@NonNull View view) {
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
  public static ViewSettingsMeBinding bind(@NonNull View view, @Nullable Object component) {
    return (ViewSettingsMeBinding)bind(component, view, R.layout.view_settings_me);
  }
}
