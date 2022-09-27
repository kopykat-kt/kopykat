package at.kopyk

/**
 * Applies 'transform' to each element in the list,
 * reusing the same structure to keep them.
 */
public fun <A> MutableList<A>.mutateAll(
  transform: (A) -> A
): MutableList<A> = apply {
  forEachIndexed { ix, value ->
    this[ix] = transform(value)
  }
}

/**
 * Applies 'transform' to each element in the list,
 * reusing the same structure to keep them.
 */
public fun <A> MutableList<A>.mutateAllIndexed(
  transform: (index: Int, value: A) -> A
): MutableList<A> = apply {
  forEachIndexed { ix, value ->
    this[ix] = transform(ix, value)
  }
}

/**
 * Applies 'transform' to each value in the map,
 * reusing the same structure to keep them.
 */
public fun <K, V> MutableMap<K, V>.mutateValues(
  transform: (entry: Map.Entry<K, V>) -> V
): MutableMap<K, V> = apply {
  forEach { entry ->
    this[entry.key] = transform(entry)
  }
}

/**
 * Applies 'transform' to each value in the map,
 * reusing the same structure to keep them.
 */
public fun <K, V> MutableMap<K, V>.mutateValues(
  transform: (key: K, value: V) -> V
): MutableMap<K, V> = apply {
  forEach { (key, value) ->
    this[key] = transform(key, value)
  }
}
