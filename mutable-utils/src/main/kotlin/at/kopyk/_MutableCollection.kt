package at.kopyk

/**
 * Applies [transform] to each element in the collection,
 * reusing the same structure to keep them.
 */
public inline fun <T : MutableCollection<A>, A> T.mutateAll(
  transform: (A) -> A,
): T = mutateAllNotNull(transform)

/**
 * Applies [transform] to each element in the collection with the current item index,
 * reusing the same structure to keep them.
 */
public inline fun <T : MutableCollection<A>, A> T.mutateAllIndexed(
  transform: (index: Int, value: A) -> A,
): T = mutateAllNotNullIndexed(transform)

/**
 * Applies [transform] to each element in the collection,
 * removing the item if `null` is returned.
 */
@Suppress("UNCHECKED_CAST") // Ah, good old erasure
public inline fun <T : MutableCollection<A>, A> T.mutateAllNotNull(
  transform: (A) -> A?,
): T = apply {
  (this as? MutableList<A>)?.mutateAllNotNull(transform) ?: clearThenAddAllNotNull(transform)
}

/**
 * Applies [transform] to each element in the list,
 * removing the item if `null` is returned.
 */
public inline fun <A> MutableList<A>.mutateAllNotNull(
  transform: (A) -> A?,
): MutableList<A> = apply {
  with(listIterator()) { while (hasNext()) transform(next())?.let(::set) ?: remove() }
}

/**
 * Removes all elements in a collection and transforms them,
 * leaving out any that are transformed to null.
 */
public inline fun <T : MutableCollection<A>, A> T.clearThenAddAllNotNull(
  transform: (A) -> A?,
): T = apply {
  val remaining = mapNotNull(transform)
  clear()
  addAll(remaining)
}

/**
 * Applies [transform] to each element in the list with the current item index,
 * removing the value if `null` is returned.
 */
public inline fun <T : MutableCollection<A>, A> T.mutateAllNotNullIndexed(
  transform: (index: Int, value: A) -> A?,
): T {
  var index = 0
  return mutateAllNotNull { transform(index++, it) }
}

/**
 * Removes any items in the list that are not type [A] and
 * returns a [MutableCollection]<[A]> with the remaining items.
 *
 * The mutable equivalent to [List.filterIsInstance]
 */
@Suppress("UNCHECKED_CAST") // The one time that type erasure is not annoying
public inline fun <reified A> MutableCollection<*>.removeUnlessInstanceOf(): MutableCollection<A> =
  (this as MutableCollection<Any?>).mutateAllNotNull { it as? A } as MutableCollection<A>

/**
 * Removes any items in the list that are not type [A] and
 * returns a [MutableList]<[A]> with the remaining items.
 *
 * The mutable equivalent to [List.filterIsInstance]
 */
@Suppress("UNCHECKED_CAST") // The one time that type erasure is not annoying
public inline fun <reified A> MutableList<*>.removeUnlessInstanceOf(): MutableList<A> =
  (this as MutableList<Any?>).mutateAllNotNull { it as? A } as MutableList<A>

/**
 * Removes any items in the list that are not type [A] and
 * returns a [MutableSet]<[A]> with the remaining items.
 *
 * The mutable equivalent to [Set.filterIsInstance]
 */
@Suppress("UNCHECKED_CAST") // The one time that type erasure is not annoying
public inline fun <reified A> MutableSet<*>.removeUnlessInstanceOf(): MutableSet<A> =
  (this as MutableSet<Any?>).mutateAllNotNull { it as? A } as MutableSet<A>
