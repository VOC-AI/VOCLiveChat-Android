
-keepattributes Signature

# Retrofit 2.X
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
 # With R8 full mode generic signatures are stripped for classes that are not
 # kept. Suspend functions are wrapped in continuations where the type argument
 # is used.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

 # R8 full mode strips generic signatures from return types if not kept.
-if interface * { @retrofit2.http.* public *** *(...); }
-keep,allowoptimization,allowshrinking,allowobfuscation class <3>

 # With R8 full mode generic signatures are stripped for classes that are not kept.
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# OkHttp 3.X
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keep public class com.squareup.okhttp.** { *; }
-keep public class org.apache.commons.logging.** { *; }
-keep class com.jakewharton.retrofit2.converter.kotlinx.serialization.** {*;}

-keepnames class com.vocai.sdk.Vocai

-keep class com.vocai.sdk.Vocai {
    <methods>;
}
#
-keep class com.vocai.sdk.Vocai$Companion {
    *;
}

# 保留所有 Companion 静态字段
-keepclassmembers class com.vocai.sdk.Vocai {
    public static **$Companion Companion;
}

# 保留所有 Companion 内部类及其成员
-keep class com.vocai.sdk.Vocai$Companion {
    *;
}

# 可选：如果 Companion 中有反射调用，保留类名
-keepnames class com.vocai.sdk.Vocai$Companion

# 或针对特定包
#-keep class com.vocai.sdk.Vocai {
#    public static ** INSTANCE;
#}

-keep class androidx.lifecycle.LiveData { *; }

# firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-keep class com.google.android.datatransport.** { *; }
-keep class com.android.installreferrer.** { *; }
-keep class com.google.android.libraries.** { *; }
-dontwarn com.google.android.libraries.**

-dontwarn com.google.android.gms.cloudmessaging.CloudMessage
-dontwarn com.google.android.gms.cloudmessaging.Rpc

# firebase-msg
-keep class com.google.firebase.messaging.** { *; }

-keep class com.google.firebase.messaging.FirebaseMessaging { *; }

# Keep generic signature of Call, Response (R8 full mode strips signatures from non-kept items).
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

 # With R8 full mode generic signatures are stripped for classes that are not
 # kept. Suspend functions are wrapped in continuations where the type argument
 # is used.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Kotlin serialization looks up the generated serializer classes through a function on companion
# objects. The companions are looked up reflectively so we need to explicitly keep these functions.
-keepclasseswithmembers class **.*$Companion {
    kotlinx.serialization.KSerializer serializer(...);
}
# If a companion has the serializer function, keep the companion field on the original type so that
# the reflective lookup succeeds.
-if class **.*$Companion {
  kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class <1>.<2> {
  <1>.<2>$Companion Companion;
}