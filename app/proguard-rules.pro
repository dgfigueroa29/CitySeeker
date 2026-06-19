# Keep source file names and line numbers for Sentry stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep BuildConfig fields
-keep class com.boa.test.city.seeker.BuildConfig { *; }

# Mapbox Maps SDK
-keep class com.mapbox.** { *; }
-dontwarn com.mapbox.**
-keep class org.maplibre.** { *; }
-dontwarn org.maplibre.**

# Retrofit
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-keepclassmembers class com.boa.test.city.seeker.data.remote.** {
    @retrofit2.http.* <methods>;
}

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.boa.test.city.seeker.data.model.** { *; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Hilt / Dagger
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Sentry
-keep class io.sentry.** { *; }
-dontwarn io.sentry.**

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}

# Kotlin Metadata
-keep class kotlin.Metadata { *; }
-keep class kotlin.jvm.internal.** { *; }

# DataStore
-keep class androidx.datastore.** { *; }

# Timber
-keep class timber.log.Timber { *; }

# LeakCanary (debug only, but keep if present)
-dontwarn com.squareup.leakcanary.**
