// Generated by data binding compiler. Do not edit!
package com.fresconews.fresco.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.Bindable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import com.fresconews.fresco.R;
import com.fresconews.fresco.v2.settings.dialogs.ChangeUserNameDialogViewModel;
import java.lang.Deprecated;
import java.lang.Object;

public abstract class ViewChangeUserNameBinding extends ViewDataBinding {
  @NonNull
  public final LinearLayout passwordLayout;

  @NonNull
  public final LinearLayout usernameLayout;

  @Bindable
  protected ChangeUserNameDialogViewModel mModel;

  protected ViewChangeUserNameBinding(Object _bindingComponent, View _root, int _localFieldCount,
      LinearLayout passwordLayout, LinearLayout usernameLayout) {
    super(_bindingComponent, _root, _localFieldCount);
    this.passwordLayout = passwordLayout;
    this.usernameLayout = usernameLayout;
  }

  public abstract void setModel(@Nullable ChangeUserNameDialogViewModel model);

  @Nullable
  public ChangeUserNameDialogViewModel getModel() {
    return mModel;
  }

  @NonNull
  public static ViewChangeUserNameBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup root, boolean attachToRoot) {
    return inflate(inflater, root, attachToRoot, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.inflate(inflater, R.layout.view_change_user_name, root, attachToRoot, component)
   */
  @NonNull
  @Deprecated
  public static ViewChangeUserNameBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup root, boolean attachToRoot, @Nullable Object component) {
    return ViewDataBinding.<ViewChangeUserNameBinding>inflateInternal(inflater, R.layout.view_change_user_name, root, attachToRoot, component);
  }

  @NonNull
  public static ViewChangeUserNameBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.inflate(inflater, R.layout.view_change_user_name, null, false, component)
   */
  @NonNull
  @Deprecated
  public static ViewChangeUserNameBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable Object component) {
    return ViewDataBinding.<ViewChangeUserNameBinding>inflateInternal(inflater, R.layout.view_change_user_name, null, false, component);
  }

  public static ViewChangeUserNameBinding bind(@NonNull View view) {
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
  public static ViewChangeUserNameBinding bind(@NonNull View view, @Nullable Object component) {
    return (ViewChangeUserNameBinding)bind(component, view, R.layout.view_change_user_name);
  }
}