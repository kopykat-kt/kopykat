package at.kopyk


/**
 * Applies [transform] to each entry in the map,
 * reusing the same structure to keep them.
 */
public inline fun <K, V> MutableMap<K, V>.mutateValues(
  transform: (entry: Map.Entry<K, V>) -> V,
): MutableMap<K, V> = mutateValuesNotNull(transform)

/**
 * Applies [transform] to each entry in the map,
 * reusing the same structure to keep them.
 */
public inline fun <K, V> MutableMap<K, V>.mutateValues(
  transform: (key: K, value: V) -> V,
): MutableMap<K, V> = mutateValuesNotNull(transform)

/**
 * Applies [transform] to each entry in the map,
 * removing the item if `null` is returned.
 */
public inline fun <K, V> MutableMap<K, V>.mutateValuesNotNull(
  transform: (entry: Map.Entry<K, V>) -> V?,
): MutableMap<K, V> = apply {
  with(iterator()) { while (hasNext()) next().apply { transform(this)?.also(::setValue) ?: remove() } }
}

/**
 * Applies [transform] to each entry in the map,
 * removing the item if `null` is returned.
 */
public inline fun <K, V> MutableMap<K, V>.mutateValuesNotNull(
  transform: (key: K, value: V) -> V?,
): MutableMap<K, V> = mutateValuesNotNull { (key, value) -> transform(key, value) }

/**
 * Removes any values in the map that are not type [V2] and
 * returns a [MutableMap]<[K], [V2]> with the remaining entries.
 */
@Suppress("UNCHECKED_CAST")
public inline fun <reified V2 : V, K, V> MutableMap<K, V>.removeValuesUnlessInstanceOf(): MutableMap<K, V2> =
  mutateValuesNotNull { (_, value) -> value as? V2 } as MutableMap<K, V2>
