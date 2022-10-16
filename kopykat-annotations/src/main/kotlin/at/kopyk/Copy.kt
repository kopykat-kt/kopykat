package at.kopyk

import kotlin.reflect.KClass

/**
 * Marks type or typealias to generate copy constructors.
 * This is the equivalent of using [CopyFrom] and [CopyTo] together.
 *
 * Both the subject of this annotation and the provided type
 * have to have the same constructor properties.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPEALIAS)
@Retention(AnnotationRetention.SOURCE)
public annotation class Copy(val type: KClass<*>)
