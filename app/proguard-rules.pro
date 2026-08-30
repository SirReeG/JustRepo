# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class ru.reeg.moexmonitor.**$$serializer { *; }
-keepclasseswithmembers class ru.reeg.moexmonitor.** {
    kotlinx.serialization.KSerializer serializer(...);
}
