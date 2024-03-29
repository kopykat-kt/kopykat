package at.kopyk.utils

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.symbol.KSAnnotated

@OptIn(KspExperimental::class)
internal inline fun <reified T : Annotation> KSAnnotated.hasAnnotation(): Boolean = getAnnotationsByType(T::class).firstOrNull() != null
