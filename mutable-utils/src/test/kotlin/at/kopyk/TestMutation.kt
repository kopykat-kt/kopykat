package at.kopyk

import io.kotest.matchers.types.shouldBeSameInstanceAs

internal fun <T> testMutation(
  given: T,
  whenWe: T.() -> T,
  then: (T) -> Unit
) {
  val result = given.whenWe()
  then(given)
  then(result)
  given shouldBeSameInstanceAs result
}
