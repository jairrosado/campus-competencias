# Serialización por reflexión no se usa: kotlinx.serialization genera los serializers
# en tiempo de compilación. Estas reglas conservan los nombres públicos por si se
# habilitan ofuscación y reportes de errores en el futuro.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class com.campus.competencias.data.model.** {
    *** Companion;
}
-keepclasseswithmembers class com.campus.competencias.data.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}
