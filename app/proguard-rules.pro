# Room Database keep rules
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep class com.example.data.** { *; }
-keepclassmembers class com.example.data.** { *; }

# Kotlin Coroutines & Reflection
-keepclassmembers class kotlinx.coroutines.** { *; }
-keepclassmembers class ** {
    @kotlinx.coroutines.InternalCoroutinesApi *;
}

# Preserve serializable models
-keepclassmembers class * implements java.io.Serializable { *; }

# Keep Compose attributes
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
