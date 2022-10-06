package at.kopyk.utils

/**
 * Makes sure the type is cast to the expected type, or fail
 */
internal inline fun <reified T> assumingCast(
  crossinline block: T.() -> Any,
): T.() -> T = { block() as T }
