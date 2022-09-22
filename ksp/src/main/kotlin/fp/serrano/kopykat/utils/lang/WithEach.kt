package fp.serrano.kopykat.utils.lang

/**
 * Alias of [forEach] with the item as the receiver.
 */
internal inline fun <A> Sequence<A>.withEach(block: A.() -> Unit) {
  forEach { it.block() }
}