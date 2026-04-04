# ============================================================
# AuraJournal ProGuard Rules
# ============================================================
# Keep line numbers for readable crash stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ============================================================
# ROOM DATABASE — Critical: R8 must NOT strip these
# ============================================================
# Keep all Room entity classes and their fields (column names must survive)
-keep class com.aadi.aurajournal.data.** { *; }

# Keep Room-generated DAO implementations (they are created at compile time
# but accessed via reflection at runtime by the Room framework)
-keep interface com.aadi.aurajournal.data.** { *; }

# Keep Room TypeConverter classes
-keepclassmembers class * {
    @androidx.room.TypeConverter <methods>;
}

# ============================================================
# GSON — Required by Converter.kt's TypeToken usage
# ============================================================
-keepattributes Signature
-keepattributes *Annotation*

# Keep Gson itself
-keep class com.google.gson.** { *; }
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken { *; }

# Keep Gson TypeAdapter and related generics
-keep class sun.misc.Unsafe { *; }
-dontwarn sun.misc.**

# ============================================================
# GOOGLE GENERATIVE AI (Gemini SDK)
# ============================================================
-keep class com.google.ai.client.generativeai.** { *; }
-dontwarn com.google.ai.client.generativeai.**

# Keep Kotlin serialization / coroutine internals used by the SDK
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# ============================================================
# COIL (Image Loading)
# ============================================================
-keep class coil.** { *; }
-dontwarn coil.**

# ============================================================
# BIOMETRIC
# ============================================================
-keep class androidx.biometric.** { *; }
-dontwarn androidx.biometric.**

# ============================================================
# KOTLIN
# ============================================================
-keep class kotlin.Metadata { *; }
-keep class kotlin.reflect.** { *; }
-dontwarn kotlin.**