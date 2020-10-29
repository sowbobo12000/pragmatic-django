# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Development/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-optimizationpasses 3

-dontwarn okio.**
-dontwarn javax.annotation.**
-keepclassmembers enum * { *; }

# Retrofit 2
# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on RoboVM on iOS. Will not be used at runtime.
-dontnote retrofit2.Platform$IOS$MainThreadExecutor
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions

-dontwarn retrofit2.adapter.rxjava.CompletableHelper$**

# OkHttp
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-keepattributes *Annotation*

# Card IO
# ---- REQUIRED card.io CONFIG ----------------------------------------
# card.io is a native lib, so anything crossing JNI must not be changed

# Don't obfuscate DetectionInfo or public fields, since
# it is used by native methods
-keep class io.card.payment.DetectionInfo
-keepclassmembers class io.card.payment.DetectionInfo {
    public *;
}
-keep class io.card.payment.CreditCard
-keep class io.card.payment.CreditCard$1
-keepclassmembers class io.card.payment.CreditCard {
  *;
}
-keepclassmembers class io.card.payment.CardScanner {
  *** onEdgeUpdate(...);
}

# Don't mess with classes with native methods
-keepclasseswithmembers class * {
    native <methods>;
}

-keepclasseswithmembernames class * {
    native <methods>;
}

-keep public class io.card.payment.* {
    public protected *;
}

# required to suppress errors when building on android 22
-dontwarn io.card.payment.CardIOActivity

# For Facebook
-keep class com.facebook.** { *; }

# For Fabric and Crashlytics
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**

# For Mixpanel
-dontwarn com.mixpanel.**

# For Data Binding
-dontwarn android.databinding.**
-keep class android.databinding.** { *; }

# For Retro Lambda
-dontwarn java.lang.invoke**

# For Google
-keep class com.google.**
-dontwarn com.google.**

# For Parcelables
-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# For Joda Time
-dontwarn org.joda.convert.FromString
-dontwarn org.joda.convert.ToString

# For Stripe
-keep class com.stripe.** { *; }

# For Gson
# Prevent proguard from stripping interface information from TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# For DBFlow
-keep class * extends com.raizlabs.android.dbflow.config.DatabaseHolder { *; }

# For Amazon
# Class names are needed in reflection
-keepnames class com.amazonaws.**
# Request handlers defined in request.handlers
-keep class com.amazonaws.services.**.*Handler
# The following are referenced but aren't required to run
-dontwarn com.fasterxml.jackson.**
-dontwarn org.apache.commons.logging.**
# Android 6.0 release removes support for the Apache HTTP client
-dontwarn org.apache.http.**
# The SDK has several references of Apache HTTP client
-dontwarn com.amazonaws.http.**
-dontwarn com.amazonaws.metrics.**

# Required to preserve the Flurry SDK
-keep class com.flurry.** { *; }
-dontwarn com.flurry.**
-keepattributes *Annotation*,EnclosingMethod,Signature
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# For Adjust
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep class com.adjust.sdk.plugin.MacAddressUtil {
    java.lang.String getMacAddress(android.content.Context);
}
-keep class com.adjust.sdk.plugin.AndroidIdUtil {
    java.lang.String getAndroidId(android.content.Context);
}
-keep class com.google.android.gms.common.ConnectionResult {
    int SUCCESS;
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient {
    com.google.android.gms.ads.identifier.AdvertisingIdClient$Info getAdvertisingIdInfo(android.content.Context);
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info {
    java.lang.String getId();
    boolean isLimitAdTrackingEnabled();
}
-keep class dalvik.system.VMRuntime {
    java.lang.String getRuntime();
}
-keep class android.os.Build {
    java.lang.String[] SUPPORTED_ABIS;
    java.lang.String CPU_ABI;
}
-keep class android.content.res.Configuration {
    android.os.LocaledList getLocales();
    java.util.Locale locale;
}
-keep class android.os.LocaledList {
    java.util.Locale get(int);
}

#----------------------------------------Zendesk-----------------------------------------#
# We only want obfuscation
-keepattributes InnerClasses
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*
-keepattributes EnclosingMethod

# Sdk
-keep public interface com.zendesk.** { *; }
-keep public class com.zendesk.** { *; }
-dontwarn java.awt.**

# Appcompat and support
-keep interface android.support.v7.** { *; }
-keep class android.support.v7.** { *; }
-keep interface android.support.v4.** { *; }
-keep class android.support.v4.** { *; }
-dontwarn android.app.Notification

# Gson
-keep interface com.google.gson.** { *; }
-keep class com.google.gson.** { *; }

# Retrofit
-keep class com.google.inject.** { *; }
-keep class org.apache.http.** { *; }
-keep class org.apache.james.mime4j.** { *; }
-keep class javax.inject.** { *; }
-keep class retrofit.** { *; }
-keep interface retrofit.** { *; }
-dontwarn rx.**
-dontwarn com.google.appengine.api.urlfetch.**
-dontwarn okio.**

-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

#Picasso
-dontwarn com.squareup.okhttp.**
#----------------------------------------Zendesk-----------------------------------------#
