# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve line numbers for crash reporting
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ---- Kotlin coroutines ----
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# ---- Google Play Billing ----
-keep class com.android.billingclient.** { *; }
-keep interface com.android.billingclient.** { *; }
-dontwarn com.android.billingclient.**

# ---- Firebase ----
-keep class com.google.firebase.** { *; }
-keep interface com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# ---- Google Sign-in / Credential Manager ----
-keep class com.google.android.libraries.identity.** { *; }
-dontwarn com.google.android.libraries.identity.**
-keep class androidx.credentials.** { *; }
-dontwarn androidx.credentials.**

# ---- Hilt ----
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keepclasseswithmembers class * {
    @dagger.hilt.android.lifecycle.HiltViewModel <init>(...);
}

# ---- Jetpack Compose ----
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ---- App ViewModels / State (prevent R8 from removing) ----
-keep,allowobfuscation,allowshrinking class pt.ms.myshare.presentation.ui.onboarding.OnboardingViewModel
-keep,allowobfuscation,allowshrinking class pt.ms.myshare.presentation.ui.home.HomeViewModel
-keep,allowobfuscation,allowshrinking class pt.ms.myshare.presentation.ui.onboarding.OnboardingState
-keep,allowobfuscation,allowshrinking class pt.ms.myshare.domain.model.** { *; }

# ---- Serialisation ----
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}