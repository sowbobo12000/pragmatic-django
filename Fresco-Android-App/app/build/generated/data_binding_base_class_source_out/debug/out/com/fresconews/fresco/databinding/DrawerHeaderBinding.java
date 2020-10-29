// Generated by data binding compiler. Do not edit!
package com.fresconews.fresco.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.Bindable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import com.fresconews.fresco.R;
import com.fresconews.fresco.v2.navdrawer.HeaderViewModel;
import java.lang.Deprecated;
import java.lang.Object;

public abstract class DrawerHeaderBinding extends ViewDataBinding {
  @NonNull
  public final TextView drawerProfileName;

  @NonNull
  public final LinearLayout loginTextButton;

  @NonNull
  public final ImageView loginTextImage;

  @NonNull
  public final LinearLayout myProfile;

  @Bindable
  protected HeaderViewModel mModel;

  protected DrawerHeaderBinding(Object _bindingComponent, View _root, int _localFieldCount,
      TextView drawerProfileName, LinearLayout loginTextButton, ImageView loginTextImage,
      LinearLayout myProfile) {
    super(_bindingComponent, _root, _localFieldCount);
    this.drawerProfileName = drawerProfileName;
    this.loginTextButton = loginTextButton;
    this.loginTextImage = loginTextImage;
    this.myProfile = myProfile;
  }

  public abstract void setModel(@Nullable HeaderViewModel model);

  @Nullable
  public HeaderViewModel getModel() {
    return mModel;
  }

  @NonNull
  public static DrawerHeaderBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup root, boolean attachToRoot) {
    return inflate(inflater, root, attachToRoot, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.inflate(inflater, R.layout.drawer_header, root, attachToRoot, component)
   */
  @NonNull
  @Deprecated
  public static DrawerHeaderBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup root, boolean attachToRoot, @Nullable Object component) {
    return ViewDataBinding.<DrawerHeaderBinding>inflateInternal(inflater, R.layout.drawer_header, root, attachToRoot, component);
  }

  @NonNull
  public static DrawerHeaderBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.inflate(inflater, R.layout.drawer_header, null, false, component)
   */
  @NonNull
  @Deprecated
  public static DrawerHeaderBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable Object component) {
    return ViewDataBinding.<DrawerHeaderBinding>inflateInternal(inflater, R.layout.drawer_header, null, false, component);
  }

  public static DrawerHeaderBinding bind(@NonNull View view) {
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
  public static DrawerHeaderBinding bind(@NonNull View view, @Nullable Object component) {
    return (DrawerHeaderBinding)bind(component, view, R.layout.drawer_header);
  }
}
