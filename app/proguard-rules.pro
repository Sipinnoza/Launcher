# 保留应用程序入口和Activity，防止被混淆
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# 保留所有带有 @Keep 注解的类和成员
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# 保留 Kotlin Metadata，防止 Kotlin 反射出错
-keep class kotlin.Metadata { *; }

# 保留序列化的类（如果你使用 Gson、Kotlinx Serialization 等）
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# 保留反射调用的类和方法（根据你项目具体情况增删）
# 举例保留你自定义的标签类、模型类等，替换为你项目对应包名和类名
-keep class com.znliang.launcher.tags.** { *; }

# 支持 Gson 反射字段
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class com.znliang.launcher.model.** { *; }

# 保留自定义视图类（防止布局加载失败）
-keep class com.znliang.launcher.tags.tag.TagCloudView { *; }

# 保留 Android Support / Jetpack 库相关类
-keep class androidx.** { *; }
-keep class com.google.android.material.** { *; }

# 关闭日志相关类（release 时删除日志）
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

