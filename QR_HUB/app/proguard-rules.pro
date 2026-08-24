# Add project specific ProGuard rules here.
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep ML Kit barcode scanning
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# Keep ZXing QR code
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**

# Keep Room database
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Keep ViewModel
-keep class * extends androidx.lifecycle.ViewModel { *; }

# Keep data classes
-keep class com.qr.hub.data.model.** { *; }
-keep class com.qr.hub.model.** { *; }