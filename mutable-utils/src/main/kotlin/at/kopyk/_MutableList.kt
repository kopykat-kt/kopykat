package at.kopyk

/**
 * Applies [transform] to each element in the list,
 * reusing the same structure to keep them.
 */
public inline fun <A> MutableList<A>.mutateAll(
  transform: (A) -> A
): MutableList<A> = apply {
  with(listIterator()) { while (hasNext()) set(transform(next())) }
}

/**
 * Applies [transform] to each element in the list with the current item index,
 * reusing the same structure to keep them.
 */
public inline fun <A> MutableList<A>.mutateAllIndexed(
  transform: (index: Int, value: A) -> A
): MutableList<A> = apply {
  with(listIterator()) { while (hasNext()) set(transform(nextIndex(), next())) }
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
 * Applies [transform] to each element in the list with the current item index,
 * removing the value if `null` is returned.
 */
public inline fun <A> MutableList<A>.mutateAllNotNullIndexed(
  transform: (index: Int, value: A) -> A?,
): MutableList<A> {
  /**
   * Implementation Note: We can't use [ListIterator.nextIndex] because
   * removing an item will change the next item index.
   */
  var index = 0
  return mutateAllNotNull { transform(index++, it) }
}
