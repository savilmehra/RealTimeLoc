# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\railyatri\AppData\Local\Android\sdk1/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
-keepclassmembers class fqcn.of.javascript.interface.for.webview {
   public *;
}



#-------------- proguard - optimize-----


# This is a configuration file for ProGuard.
# http://proguard.sourceforge.net/index.html#manual/usage.html

# Optimizations: If you don't want to optimize, use the
# proguard-android.txt configuration file instead of this one, which
# turns off the optimization flags.  Adding optimization introduces
# certain risks, since for example not all optimizations performed by
# ProGuard works on all versions of Dalvik.  The following flags turn
# off various optimizations known to have issues, but the list may not
# be complete or up to date. (The "arithmetic" optimization can be
# used if you are only targeting Android 2.0 or later.)  Make sure you
# test thoroughly if you go this route.
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# The remainder of this file is identical to the non-optimized version
# of the Proguard configuration file (except that the other file has
# flags to turn off optimization).
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

-keepattributes *Annotation*
-keep public class com.google.vending.licensing.ILicensingService
-keep public class com.android.vending.licensing.ILicensingService

# For native methods, see http://proguard.sourceforge.net/manual/examples.html#native
-keepclasseswithmembernames class * {
    native <methods>;
}
-keepattributes JavascriptInterface

# keep setters in Views so that animations can still work.
# see http://proguard.sourceforge.net/manual/examples.html#beans
-keepclassmembers public class * extends android.view.View {
   void set*(***);
   *** get*();
}

# We want to keep methods in Activity that could be used in the XML attribute onClick
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator CREATOR;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}


# for retrofit
# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions


##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { *; }

-keepclassmembers enum * { *; }


# proguard configurations for Crashlytics

-keep public class * extends java.lang.Exception
-keepattributes SourceFile,LineNumberTable

# proguard configurations for Design Support Library

-keep public class * extends android.support.design.widget.CoordinatorLayout$Behavior { *; }

# proguard configuration for RailYatri
#-keep class com.railyatri.in.entities.**{*;}
#-keep class com.railyatri.in.pg.**{*;}
#-keep class com.railyatri.in.localtrains.**{*;}
#-keep class com.railyatri.in.helper.**{*;}
#
#-keep class com.railyatri.in.adapters.**{*;}
#-keep class com.railyatri.in.retrofitentities.**{*;}
#-keep class com.railyatri.in.common.**{*;}
#-keep class com.railyatri.in.speedometer.**{*;}
-keepclassmembers class com.railyatri.in.bus.bus_activity.BusSeatSelectionActivity$JavaScriptInterface{public *;}
-keepclassmembers class com.railyatri.in.bus.bus_fragments.BusSeatSelectionFragment$JavaScriptInterface{public *;}
-keepclassmembers class com.railyatri.in.packages.PackagesWebViewActivity$JavaScriptInterface{public *;}
-keepclassmembers class com.railyatri.in.webviewgeneric.JavaScriptInterfaceGeneric{public *;}
#-keepclassmembers class com.paytm.pgsdk.PaytmWebView$PaytmJavaScriptInterface {
#   public *;
#}

# proguard config for event bus
-keepclassmembers class ** {
   public void onEvent*(**);
}

# proguard for support libraries
#-keep class android.support.v4.** { *; }
#-keep class android.support.v7.** { *; }

-keep public class android.util.FloatMath
#-keep class com.polites.android.MathUtils.**
#-dontwarn class com.polites.android.MathUtils.**

# proguard config for Serializable
# http://stackoverflow.com/a/6780028/3676200
-keep class * implements java.io.Serializable

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keep public class com.railyatri.in.packages.PackageSendDataEntity** { *; }

-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

-keepattributes InnerClasses
-dontoptimize

# configurations for glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-dontwarn com.bumptech.glide.load.resource.bitmap.VideoDecoder

# For CleverTap SDK
-dontwarn com.clevertap.android.sdk.**
-dontwarn com.razorpay.**
-keep class com.razorpay.** {*;}

-optimizations !method/inlining/*

-keepclasseswithmembers class * {
  public void onPayment*(...);

}

-keep public class * extends com.railyatri.in.DynamicHome.Provider.*

-keepclassmembers class * extends com.railyatri.in.DynamicHome.Provider.*{
 public <init>(android.content.Context);
}

-keep public class com.google.firebase.analytics.FirebaseAnalytics {
    public *;
}


-keep public class com.google.android.gms.measurement.AppMeasurement {
    public *;
}

-keep class com.truecaller.** {*;}

-keep class com.github.anrwatchdog.** { *; }
-dontwarn com.android.installreferrer

# Proguard configuration for Jackson 2.x (fasterxml package instead of codehaus package)
-keep class com.fasterxml.jackson.databind.ObjectMapper {
    public <methods>;
    protected <methods>;
}
-keep class com.fasterxml.jackson.databind.ObjectWriter {
    public ** writeValueAsString(**);
}
-keepnames class com.fasterxml.jackson.** { *; }
-dontwarn com.fasterxml.jackson.databind.**

-keep class com.google.obf.** { *; }
-keep interface com.google.obf.** { *; }

-keep class com.google.ads.interactivemedia.** { *; }
-keep interface com.google.ads.interactivemedia.** { *; }

-dontwarn com.warkiz.widget.**

-dontwarn com.crashlytics.android.answers.shim.**

-keep class com.google.android.gms.measurement.AppMeasurement { *; }
-keep class com.google.android.gms.measurement.AppMeasurement$OnEventListener { *; }

-keep class com.bumptech.glide.integration.okhttp3.OkHttpGlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep class com.bumptech.glide.GeneratedAppGlideModuleImpl


-keep class com.appsflyer.** { *; }
-dontwarn com.appsflyer.**

-keep public class com.google.android.gms.* { public *; }
-dontwarn com.google.android.gms.**

#-dontwarn okhttp3.internal.**
#-dontwarn okio.**

# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*

# OkHttp platform used only on JVM and when Conscrypt dependency is available.
-dontwarn okhttp3.internal.platform.ConscryptPlatform

-keep class ru.ivanarh.jndcrash.** {*;}

# For facebook crypto library
# Keep our interfaces so they can be used by other ProGuard rules.
# See http://sourceforge.net/p/proguard/bugs/466/
-keep,allowobfuscation @interface com.facebook.crypto.proguard.annotations.DoNotStrip
-keep,allowobfuscation @interface com.facebook.crypto.proguard.annotations.KeepGettersAndSetters

# Do not strip any method/class that is annotated with @DoNotStrip
-keep @com.facebook.crypto.proguard.annotations.DoNotStrip class *
-keepclassmembers class * {
  @com.facebook.crypto.proguard.annotations.DoNotStrip *;
}

-keepclassmembers @com.facebook.crypto.proguard.annotations.KeepGettersAndSetters class * {
 void set*(***);
 *** get*();
}

-dontwarn android.webkit.**

-keep class com.esotericsoftware.** {*;}
-dontwarn com.esotericsoftware.**

-keep class in.railyatri.ltslib.** {*;}
-dontwarn in.railyatri.ltslib.**

-keep class de.javakaffee.kryoserializers.**{*;}
-dontwarn de.javakaffee.kryoserializers.**

-keep class com.railyatri.in.train_status.**{*;}
-dontwarn com.railyatri.in.train_status.**

-keep class com.railyatri.in.livetrainstatus.views.**{*;}

-keep class com.railyatri.in.livetrainstatus.entities.**{*;}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keep class com.railyatri.in.pnr.scraper.**{*;}
-dontwarn com.railyatri.in.pnr.scraper.**

# -keep class com.google.android.apps.**{*;}
# -dontwarn com.google.android.apps.**


-keep class com.railyatri.in.pg.gpay.**{*;}
-dontwarn com.google.firebase.appindexing.internal.zzab

#-dontwarn org.joda.convert.**
#-dontwarn org.joda.time.**
#-keep class org.joda.time.** { *; }
#-keep interface org.joda.time.** { *; }
#-dontwarn org.joda.convert.FromString
#-dontwarn org.joda.convert.ToString


-dontwarn com.sothree.**
-keep class com.sothree.**
-keep interface com.sothree.**