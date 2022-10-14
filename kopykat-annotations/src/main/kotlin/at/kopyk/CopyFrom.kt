package at.kopyk

import kotlin.reflect.KClass

/**
 * Marks type or typealias to generate copy constructor from
 * the provided [type] to the subject of this annotation.
 *
 * Both the subject of this annotation and the provided type
 * have to have the same constructor properties.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPEALIAS)
@Retention(AnnotationRetention.SOURCE)
public annotation class CopyFrom(val type: KClass<*>)


