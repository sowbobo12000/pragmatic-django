// Generated by data binding compiler. Do not edit!
package com.fresconews.fresco.databinding;

import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.Bindable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import com.fresconews.fresco.R;
import com.fresconews.fresco.v2.editprofile.EditProfileViewModel;
import java.lang.Deprecated;
import java.lang.Object;

public abstract class ActivityEditProfileBinding extends ViewDataBinding {
  @NonNull
  public final FrameLayout avatar;

  @NonNull
  public final TextInputLayout bioLayout;

  @NonNull
  public final TextInputEditText editBio;

  @NonNull
  public final TextInputEditText editLocation;

  @NonNull
  public final TextInputEditText editName;

  @NonNull
  public final TextInputLayout locationLayout;

  @NonNull
  public final TextInputLayout nameLayout;

  @NonNull
  public final RelativeLayout profileFooter;

  @NonNull
  public final Toolbar toolbar;

  @Bindable
  protected EditProfileViewModel mModel;

  protected ActivityEditProfileBinding(Object _bindingComponent, View _root, int _localFieldCount,
      FrameLayout avatar, TextInputLayout bioLayout, TextInputEditText editBio,
      TextInputEditText editLocation, TextInputEditText editName, TextInputLayout locationLayout,
      TextInputLayout nameLayout, RelativeLayout profileFooter, Toolbar toolbar) {
    super(_bindingComponent, _root, _localFieldCount);
    this.avatar = avatar;
    this.bioLayout = bioLayout;
    this.editBio = editBio;
    this.editLocation = editLocation;
    this.editName = editName;
    this.locationLayout = locationLayout;
    this.nameLayout = nameLayout;
    this.profileFooter = profileFooter;
    this.toolbar = toolbar;
  }

  public abstract void setModel(@Nullable EditProfileViewModel model);

  @Nullable
  public EditProfileViewModel getModel() {
    return mModel;
  }

  @NonNull
  public static ActivityEditProfileBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup root, boolean attachToRoot) {
    return inflate(inflater, root, attachToRoot, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.inflate(inflater, R.layout.activity_edit_profile, root, attachToRoot, component)
   */
  @NonNull
  @Deprecated
  public static ActivityEditProfileBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup root, boolean attachToRoot, @Nullable Object component) {
    return ViewDataBinding.<ActivityEditProfileBinding>inflateInternal(inflater, R.layout.activity_edit_profile, root, attachToRoot, component);
  }

  @NonNull
  public static ActivityEditProfileBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.inflate(inflater, R.layout.activity_edit_profile, null, false, component)
   */
  @NonNull
  @Deprecated
  public static ActivityEditProfileBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable Object component) {
    return ViewDataBinding.<ActivityEditProfileBinding>inflateInternal(inflater, R.layout.activity_edit_profile, null, false, component);
  }

  public static ActivityEditProfileBinding bind(@NonNull View view) {
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
  public static ActivityEditProfileBinding bind(@NonNull View view, @Nullable Object component) {
    return (ActivityEditProfileBinding)bind(component, view, R.layout.activity_edit_profile);
  }
}
