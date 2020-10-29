// Generated by data binding compiler. Do not edit!
package com.fresconews.fresco.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.Bindable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import com.facebook.drawee.view.SimpleDraweeView;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.mvvm.viewmodels.UserSearchViewModel;
import java.lang.Deprecated;
import java.lang.Object;

public abstract class ItemUserBinding extends ViewDataBinding {
  @NonNull
  public final RelativeLayout user;

  @NonNull
  public final SimpleDraweeView userAvatar;

  @NonNull
  public final TextView userFullname;

  @NonNull
  public final TextView userUsername;

  @Bindable
  protected UserSearchViewModel mModel;

  protected ItemUserBinding(Object _bindingComponent, View _root, int _localFieldCount,
      RelativeLayout user, SimpleDraweeView userAvatar, TextView userFullname,
      TextView userUsername) {
    super(_bindingComponent, _root, _localFieldCount);
    this.user = user;
    this.userAvatar = userAvatar;
    this.userFullname = userFullname;
    this.userUsername = userUsername;
  }

  public abstract void setModel(@Nullable UserSearchViewModel model);

  @Nullable
  public UserSearchViewModel getModel() {
    return mModel;
  }

  @NonNull
  public static ItemUserBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup root,
      boolean attachToRoot) {
    return inflate(inflater, root, attachToRoot, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.inflate(inflater, R.layout.item_user, root, attachToRoot, component)
   */
  @NonNull
  @Deprecated
  public static ItemUserBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup root,
      boolean attachToRoot, @Nullable Object component) {
    return ViewDataBinding.<ItemUserBinding>inflateInternal(inflater, R.layout.item_user, root, attachToRoot, component);
  }

  @NonNull
  public static ItemUserBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.inflate(inflater, R.layout.item_user, null, false, component)
   */
  @NonNull
  @Deprecated
  public static ItemUserBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable Object component) {
    return ViewDataBinding.<ItemUserBinding>inflateInternal(inflater, R.layout.item_user, null, false, component);
  }

  public static ItemUserBinding bind(@NonNull View view) {
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
  public static ItemUserBinding bind(@NonNull View view, @Nullable Object component) {
    return (ItemUserBinding)bind(component, view, R.layout.item_user);
  }
}