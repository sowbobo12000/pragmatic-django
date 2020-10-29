// Generated by data binding compiler. Do not edit!
package com.fresconews.fresco.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import com.fresconews.fresco.R;
import java.lang.Deprecated;
import java.lang.Object;

public abstract class ItemPaymentMethodBinding extends ViewDataBinding {
  @NonNull
  public final RadioButton activePaymentButton;

  @NonNull
  public final ImageButton paymentDeleteButton;

  @NonNull
  public final LinearLayout paymentMethodLinearLayout;

  @NonNull
  public final TextView paymentTypeTextView;

  protected ItemPaymentMethodBinding(Object _bindingComponent, View _root, int _localFieldCount,
      RadioButton activePaymentButton, ImageButton paymentDeleteButton,
      LinearLayout paymentMethodLinearLayout, TextView paymentTypeTextView) {
    super(_bindingComponent, _root, _localFieldCount);
    this.activePaymentButton = activePaymentButton;
    this.paymentDeleteButton = paymentDeleteButton;
    this.paymentMethodLinearLayout = paymentMethodLinearLayout;
    this.paymentTypeTextView = paymentTypeTextView;
  }

  @NonNull
  public static ItemPaymentMethodBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup root, boolean attachToRoot) {
    return inflate(inflater, root, attachToRoot, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.inflate(inflater, R.layout.item_payment_method, root, attachToRoot, component)
   */
  @NonNull
  @Deprecated
  public static ItemPaymentMethodBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup root, boolean attachToRoot, @Nullable Object component) {
    return ViewDataBinding.<ItemPaymentMethodBinding>inflateInternal(inflater, R.layout.item_payment_method, root, attachToRoot, component);
  }

  @NonNull
  public static ItemPaymentMethodBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.inflate(inflater, R.layout.item_payment_method, null, false, component)
   */
  @NonNull
  @Deprecated
  public static ItemPaymentMethodBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable Object component) {
    return ViewDataBinding.<ItemPaymentMethodBinding>inflateInternal(inflater, R.layout.item_payment_method, null, false, component);
  }

  public static ItemPaymentMethodBinding bind(@NonNull View view) {
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
  public static ItemPaymentMethodBinding bind(@NonNull View view, @Nullable Object component) {
    return (ItemPaymentMethodBinding)bind(component, view, R.layout.item_payment_method);
  }
}