package at.kopyk

/**
 * Applies 'transform' to each element in the list,
 * reusing the same structure to keep them.
 */
public fun <A> MutableList<A>.mapInPlace(
  transform: (A) -> A
): MutableList<A> {
  forEachIndexed { ix, value ->
    this[ix] = transform(value)
  }
  return this
}

/**
 * Applies 'transform' to each element in the list,
 * reusing the same structure to keep them.
 */
public fun <A> MutableList<A>.mapIndexedInPlace(
  transform: (index: Int, value: A) -> A
): MutableList<A> {
  forEachIndexed { ix, value ->
    this[ix] = transform(ix, value)
  }
  return this
}

/**
 * Applies 'transform' to each value in the map,
 * reusing the same structure to keep them.
 */
public fun <K, V> MutableMap<K, V>.mapValuesInPlace(
  transform: (entry: Map.Entry<K, V>) -> V
): MutableMap<K, V> {
  forEach { entry ->
    this[entry.key] = transform(entry)
  }
  return this
}

/**
 * Applies 'transform' to each value in the map,
 * reusing the same structure to keep them.
 */
public fun <K, V> MutableMap<K, V>.mapValuesInPlace(
  transform: (key: K, value: V) -> V
): MutableMap<K, V> {
  forEach { (key, value) ->
    this[key] = transform(key, value)
  }
  return this
}
