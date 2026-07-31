-dontwarn **
-dontnote **

-dontoptimize
-dontshrink

-keepattributes Signature,InnerClasses,EnclosingMethod,*Annotation*

-keep class !org.trivait.minigamesmod.leaderboard.** { *; }
-keepclassmembers class !org.trivait.minigamesmod.leaderboard.** { *; }

-keepclasseswithmembers class * {
    public static void main(java.lang.String[]);
}

-useuniqueclassmembernames
-overloadaggressively
-allowaccessmodification