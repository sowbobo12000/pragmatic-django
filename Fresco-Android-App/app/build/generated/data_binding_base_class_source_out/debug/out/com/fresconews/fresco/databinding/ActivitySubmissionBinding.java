// Generated by data binding compiler. Do not edit!
package com.fresconews.fresco.databinding;

import android.support.v4.widget.NestedScrollView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.Bindable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import com.fresconews.fresco.R;
import com.fresconews.fresco.v2.submission.SubmissionViewModel;
import java.lang.Deprecated;
import java.lang.Object;

public abstract class ActivitySubmissionBinding extends ViewDataBinding {
  @NonNull
  public final EditText captionEditText;

  @NonNull
  public final NestedScrollView contentList;

  @NonNull
  public final RadioButton radioButton;

  @NonNull
  public final FrameLayout submitFooter;

  @Bindable
  protected SubmissionViewModel mModel;

  protected ActivitySubmissionBinding(Object _bindingComponent, View _root, int _localFieldCount,
      EditText captionEditText, NestedScrollView contentList, RadioButton radioButton,
      FrameLayout submitFooter) {
    super(_bindingComponent, _root, _localFieldCount);
    this.captionEditText = captionEditText;
    this.contentList = contentList;
    this.radioButton = radioButton;
    this.submitFooter = submitFooter;
  }

  public abstract void setModel(@Nullable SubmissionViewModel model);

  @Nullable
  public SubmissionViewModel getModel() {
    return mModel;
  }

  @NonNull
  public static ActivitySubmissionBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup root, boolean attachToRoot) {
    return inflate(inflater, root, attachToRoot, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.inflate(inflater, R.layout.activity_submission, root, attachToRoot, component)
   */
  @NonNull
  @Deprecated
  public static ActivitySubmissionBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup root, boolean attachToRoot, @Nullable Object component) {
    return ViewDataBinding.<ActivitySubmissionBinding>inflateInternal(inflater, R.layout.activity_submission, root, attachToRoot, component);
  }

  @NonNull
  public static ActivitySubmissionBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, DataBindingUtil.getDefaultComponent());
  }

  /**
   * This method receives DataBindingComponent instance as type Object instead of
   * type DataBindingComponent to avoid causing too many compilation errors if
   * compilation fails for another reason.
   * https://issuetracker.google.com/issues/116541301
   * @Deprecated Use DataBindingUtil.inflate(inflater, R.layout.activity_submission, null, false, component)
   */
  @NonNull
  @Deprecated
  public static ActivitySubmissionBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable Object component) {
    return ViewDataBinding.<ActivitySubmissionBinding>inflateInternal(inflater, R.layout.activity_submission, null, false, component);
  }

  public static ActivitySubmissionBinding bind(@NonNull View view) {
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
  public static ActivitySubmissionBinding bind(@NonNull View view, @Nullable Object component) {
    return (ActivitySubmissionBinding)bind(component, view, R.layout.activity_submission);
  }
}
