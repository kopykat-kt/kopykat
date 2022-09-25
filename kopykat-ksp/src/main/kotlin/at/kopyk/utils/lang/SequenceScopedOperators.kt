package at.kopyk.utils.lang

/**
 * Alias of [forEach] with the item as the receiver.
 */
internal inline fun <A> Sequence<A>.forEachRun(block: A.() -> Unit) {
  forEach(block)
}

/**
 * Alias of [onEach] with the item as the receiver.
 */
internal fun <A> Sequence<A>.onEachRun(block: A.() -> Unit) = onEach(block)

/**
 * Alias of [map] with the item as the receiver.
 */
internal fun <A, R> Sequence<A>.mapRun(block: A.() -> R) = map(block)
