# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\go80.jung\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

-dontwarn org.cybergarage.**
-keep class org.cybergarage.** {*;}
-keep public interface org.cybergarage.** {*;}

-keep class com.googlecode.mp4parser.** {*;}
-dontwarn com.googlecode.mp4parser.**

-keep,includedescriptorclasses public class com.samsung.android.sdk.gear360.SGear360 {*;}
-keep,includedescriptorclasses public class com.samsung.android.sdk.gear360.SGear360Exception {*;}
-keep,includedescriptorclasses public class com.samsung.android.sdk.gear360.device.camera.Camera {*;}
-keep,includedescriptorclasses public class com.samsung.android.sdk.gear360.device.ConnectionManager {*;}
-keep,includedescriptorclasses public class com.samsung.android.sdk.gear360.device.DeviceInfo {*;}
-keep,includedescriptorclasses public class com.samsung.android.sdk.gear360.device.Discovery {*;}
-keep,includedescriptorclasses public class com.samsung.android.sdk.gear360.device.FileManager {*;}
-keep,includedescriptorclasses public class com.samsung.android.sdk.gear360.device.Setting {*;}
-keep,includedescriptorclasses public class com.samsung.android.sdk.gear360.device.VideoPlayer {*;}
-keep,includedescriptorclasses public class com.samsung.android.sdk.gear360.device.data.** {*;}

-keep public interface com.samsung.android.** {*;}
-keep public enum com.samsung.android.** {*;}

-keep class com.samsung.android.sdk.gear360.core.connection.rfcomm.RFCOMMConnectionImpl {*;}
-keep public class com.samsung.android.meta360.XmpUtil {*;}
-keep public class com.samsung.android.secvision.stitch360.** {*;}

-dontnote com.samsung.android.meta360.**

# Don't note duplicate definition (Legacy Apche Http Client)
-dontnote android.net.http.*
-dontnote org.apache.http.**

-keep public class * extends android.content.ServiceConnection
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.preference.Preference
-keep public class * extends android.os.Binder

-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod

-keep public class * { protected *; }

-keepclassmembernames class * {
    java.lang.Class class$(java.lang.String);
    java.lang.Class class$(java.lang.String, boolean);
    native <methods>;
}

-keepclassmembers,allowoptimization enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}