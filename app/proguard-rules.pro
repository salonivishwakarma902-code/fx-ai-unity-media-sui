# Add project specific ProGuard rules here for Google Play Store Release (AAB / APK).
-keepattributes SourceFile,LineNumberTable
-keepattributes Signature,InnerClasses,EnclosingMethod
-keepattributes *Annotation*

# Keep Firestore & Pixora Data Models for reflection serialization
-keep class com.example.data.model.** { *; }

# Firebase & Google Play Services Credential Manager
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# OkHttp & Networking
-dontwarn okhttp3.**
-dontwarn okio.**
