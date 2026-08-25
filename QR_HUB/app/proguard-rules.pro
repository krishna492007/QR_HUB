# ProGuard / R8 Rules for QR HUB

# ── Keep All App Core, UI, Models & ViewModels ──
-keep class com.qr.hub.** { *; }
-keepclassmembers class com.qr.hub.** { *; }

# ── Keep ML Kit Barcode Scanning ──
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.vision.** { *; }
-dontwarn com.google.mlkit.**

# ── Keep ZXing QR Code Engine ──
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**

# ── Keep Room Database ──
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ── Keep Jetpack Compose & Material3 ──
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ── Keep Kotlin Coroutines & Serialization ──
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepattributes *Annotation*, InnerClasses, Signature
-dontnote kotlinx.serialization.AnnotationsKt

# ── Keep CameraX & Lifecycle ──
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**
-dontwarn com.google.common.**

# ── Keep Start.io SDK ──
-keep class com.startapp.** { *; }
-dontwarn com.startapp.**

# ── Keep Unity Ads SDK ──
-keep class com.unity3d.ads.** { *; }
-keep class com.unity3d.services.** { *; }
-dontwarn com.unity3d.**

# ── Keep Google AdMob SDK ──
-keep class com.google.android.gms.ads.** { *; }
-dontwarn com.google.android.gms.ads.**