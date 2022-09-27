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
