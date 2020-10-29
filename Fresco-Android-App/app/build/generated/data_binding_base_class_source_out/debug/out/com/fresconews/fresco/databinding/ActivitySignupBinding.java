// Generated by data binding compiler. Do not edit!
package com.fresconews.fresco.databinding;

import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.Bindable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import com.fresconews.fresco.R;
import com.fresconews.fresco.v2.signup.SignupViewModel;
import java.lang.Deprecated;
import java.lang.Object;

public abstract class ActivitySignupBinding extends ViewDataBinding {
  @NonNull
  public final TextView bottomPromoCode;

  @NonNull
  public final TextView bottomServiceText;

  @NonNull
  public final ImageButton facebookButton;

  @NonNull
  public final ImageButton googleButton;

  @NonNull
  public final LinearLayout mapLayout;

  @NonNull
  public final TextView mapText;

  @NonNull
  public final TextView mapTextHeader;

  @NonNull
  public final TextInputLayout passwordTextInputLayout;

  @NonNull
  public final EditText promoCode;

  @NonNull
  public final Toolbar toolbar;

  @NonNull
  public final ImageButton twitterButton;

  @Bindable
  protected SignupViewModel mModel;

  protected ActivitySignupBinding(Object _bindingComponent, View _root, int _localFieldCount,
      TextView bottomPromoCode, TextView bottomServiceText, ImageButton facebookButton,
      ImageButton googleButton, LinearLayout mapLayout, TextView mapText, TextView mapTextHeader,
      TextInputLayout passwordTextInputLayout, EditText promoCode, Toolbar toolbar,
      ImageButton twitterButton) {
    super(_bindingComponent, _root, _localFieldCount);
    this.bottomPromoCode = bottomPromoCode;
    this.bottomServiceText = bottomServiceText;
    this.facebookButton = facebookButton;
    this.googleButton = googleButton;
    this.mapLayout = mapLayout;
    this.mapText = mapText;
    this.mapTextHeader = mapTextHeader;
    this.passwordTextInputLayout = passwordTextInputLayout;
    this.promoCode = promoCode;
    this.toolbar = toolbar;
    this.twitterButton = twitterButton;
  }

  public abstract void setModel(@Nullable SignupViewModel model);

  @Nullable
  public SignupViewModel getModel() {
    return mModel;
  }

  @NonNull
  public static ActivitySignupBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup root, boolean attachToRoot) {
    return inflate(inflater, root, attachToRoot, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.inflate(inflater, R.layout.activity_signup, root, attachToRoot, component)
   */
  @NonNull
  @Deprecated
  public static ActivitySignupBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup root, boolean attachToRoot, @Nullable Object component) {
    return ViewDataBinding.<ActivitySignupBinding>inflateInternal(inflater, R.layout.activity_signup, root, attachToRoot, component);
  }

  @NonNull
  public static ActivitySignupBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.inflate(inflater, R.layout.activity_signup, null, false, component)
   */
  @NonNull
  @Deprecated
  public static ActivitySignupBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable Object component) {
    return ViewDataBinding.<ActivitySignupBinding>inflateInternal(inflater, R.layout.activity_signup, null, false, component);
  }

  public static ActivitySignupBinding bind(@NonNull View view) {
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
  public static ActivitySignupBinding bind(@NonNull View view, @Nullable Object component) {
    return (ActivitySignupBinding)bind(component, view, R.layout.activity_signup);
  }
}