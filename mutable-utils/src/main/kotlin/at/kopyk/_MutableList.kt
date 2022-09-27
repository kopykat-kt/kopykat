package at.kopyk

/**
 * Applies [transform] to each element in the list,
 * reusing the same structure to keep them.
 */
public inline fun <A> MutableList<A>.mutateAll(
  transform: (A) -> A,
): MutableList<A> = mutateAllNotNull(transform)

/**
 * Applies [transform] to each element in the list with the current item index,
 * reusing the same structure to keep them.
 */
public inline fun <A> MutableList<A>.mutateAllIndexed(
  transform: (index: Int, value: A) -> A,
): MutableList<A> = mutateAllNotNullIndexed(transform)

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
 * Applies [transform] to each element in the list with the current item index,
 * removing the value if `null` is returned.
 */
public inline fun <A> MutableList<A>.mutateAllNotNullIndexed(
  transform: (index: Int, value: A) -> A?,
): MutableList<A> {
  var index = 0
  return mutateAllNotNull { transform(index++, it) }
}

/**
 * Removes any items in the list that are not type [A] and
 * returns a [MutableList]<[A]> with the remaining items.
 *
 * The mutable equivalent to [List.filterIsInstance]
 */
@Suppress("UNCHECKED_CAST") // The one time that I found type erasure not annoying
public inline fun <reified A> MutableList<*>.removeUnlessInstanceOf(): MutableList<A> =
  (this as MutableList<Any?>).mutateAllNotNull { it as? A } as MutableList<A>
