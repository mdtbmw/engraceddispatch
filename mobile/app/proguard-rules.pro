# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# WebView JavaScript Interface Keep Rules
# Paystack uses inline Javascript callback handlers inside the WebView.
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keepattributes JavascriptInterface
-keepattributes *Annotation*

# Preserve the line number information for debugging stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Room Database Keep Rules
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep class com.example.data.** { *; }

# Moshi Serialization & JSON Parsing Keep Rules
-keep class com.example.data.Models** { *; }
-keep class com.example.data.AIModels** { *; }
-keep class * {
    @com.squareup.moshi.Json <fields>;
}
-keep class * {
    @com.squareup.moshi.JsonClass <fields>;
}

# Retrofit / OkHttp Rules
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature, InnerClasses, EnclosingMethod
-dontwarn okhttp3.**
-dontwarn okio.**
-keepnames class okhttp3.** { *; }

# Firebase Keep Rules (Auth, Firestore, Messaging, AI)
-dontwarn com.google.firebase.**
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Coil Image Loader Keep Rules
-dontwarn coil.**
-keep class coil.** { *; }

# Kotlin Coroutines Keep Rules
-dontwarn kotlinx.coroutines.**
-keep class kotlinx.coroutines.** { *; }

# Tink Cryptography / Missing Optional Dependencies
-dontwarn com.google.crypto.tink.util.KeysDownloader
-dontwarn com.google.api.client.http.**
-dontwarn org.joda.time.**

# Keep all classes, interfaces, and members in our project to prevent reflection/serialization crashes in release minification
-keep class com.example.** { *; }
-keep interface com.example.** { *; }
-keepclassmembers class com.example.** { *; }
-keepattributes Signature, *Annotation*, InnerClasses, EnclosingMethod



