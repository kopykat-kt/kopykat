package at.kopyk

import kotlin.reflect.KClass

/**
 * Marks type or typealias to generate copy constructor from
 * the subject of this annotation to the provided [type].
 *
 * Both the subject of this annotation and the provided type
 * have to have the same constructor properties.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPEALIAS)
@Retention(AnnotationRetention.SOURCE)
public annotation class CopyTo(val type: KClass<*>)
