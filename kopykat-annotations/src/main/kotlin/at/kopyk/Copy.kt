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
