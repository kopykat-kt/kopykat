package at.kopyk

/**
 * Applies [transform] to each entry in the map,
 * reusing the same structure to keep them.
 *
 * The mutable equivalent to [Map.mapValues].
 */
public inline fun <K, V> MutableMap<K, V>.mutateValues(
  transform: (entry: Map.Entry<K, V>) -> V
): MutableMap<K, V> = mutateValuesNotNull(transform)

/**
 * Applies [transform] to each entry in the map,
 * reusing the same structure to keep them.
 *
 * The mutable equivalent to [Map.mapValues].
 */
public inline fun <K, V> MutableMap<K, V>.mutateValues(
  transform: (key: K, value: V) -> V
): MutableMap<K, V> = mutateValuesNotNull(transform)

/**
 * Applies [transform] to each entry in the map,
 * removing the item if `null` is returned.
 */
public inline fun <K, V> MutableMap<K, V>.mutateValuesNotNull(
  transform: (entry: Map.Entry<K, V>) -> V?
): MutableMap<K, V> = apply {
  with(iterator()) {
    while (hasNext()) {
      next().apply {
        transform(this)?.also(::setValue) ?: remove()
      }
    }
  }
}

/**
 * Applies [transform] to each entry in the map,
 * removing the item if `null` is returned.
 */
public inline fun <K, V> MutableMap<K, V>.mutateValuesNotNull(
  transform: (key: K, value: V) -> V?
): MutableMap<K, V> = mutateValuesNotNull { (key, value) -> transform(key, value) }

/**
 * Removes any values in the map that are not type [V] and
 * returns a [MutableMap]<[K], [V]> with the remaining entries.
 */
@Suppress("UNCHECKED_CAST")
public inline fun <K, reified V> MutableMap<K, *>.removeValuesUnlessInstanceOf(): MutableMap<K, V> =
  (this as MutableMap<Any, Any>).mutateValuesNotNull { (_, value) -> value as? V } as MutableMap<K, V>
