package at.kopyk

/**
 * Applies [transform] to each element in the list,
 * reusing the same structure to keep them.
 */
public inline fun <A> MutableSet<A>.mutateAll(
  transform: (A) -> A,
): MutableSet<A> = mutateAllNotNull(transform)

/**
 * Applies [transform] to each element in the list with the current item index,
 * reusing the same structure to keep them.
 */
public inline fun <A> MutableSet<A>.mutateAllIndexed(
  transform: (index: Int, value: A) -> A,
): MutableSet<A> = mutateAllNotNullIndexed(transform)

/**
 * Applies [transform] to each element in the list,
 * removing the item if `null` is returned.
 */
public inline fun <A> MutableSet<A>.mutateAllNotNull(
  transform: (A) -> A?,
): MutableSet<A> = apply {
  val remaining = mapNotNull(transform)
  clear()
  addAll(remaining)
}

/**
 * Applies [transform] to each element in the list with the current item index,
 * removing the value if `null` is returned.
 */
public inline fun <A> MutableSet<A>.mutateAllNotNullIndexed(
  transform: (index: Int, value: A) -> A?,
): MutableSet<A> {
  var index = 0
  return mutateAllNotNull { transform(index++, it) }
}

/**
 * Removes any items in the list that are not type [A] and
 * returns a [MutableSet]<[A]> with the remaining items.
 *
 * The mutable equivalent to [Set.filterIsInstance]
 */
@Suppress("UNCHECKED_CAST") // The one time that I found type erasure not annoying
public inline fun <reified A> MutableSet<*>.removeUnlessInstanceOf(): MutableSet<A> =
  (this as MutableSet<Any?>).mutateAllNotNull { it as? A } as MutableSet<A>
