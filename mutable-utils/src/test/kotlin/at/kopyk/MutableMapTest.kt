package at.kopyk

import at.kopyk.utils.assumingCast
import io.kotest.matchers.maps.shouldContainExactly
import org.junit.jupiter.api.Test

class MutableMapTest {
  @Test
  fun `can mutate all entries`() =
    testMutation(
      given = mutableMapOf(12 to "a", 15 to "b"),
      whenWe = { mutateValues { (k, v) -> "$k -> $v" } },
      then = { it shouldContainExactly mapOf(12 to "12 -> a", 15 to "15 -> b") },
    )

  @Test
  fun `can mutate all items deconstructed`() =
    testMutation(
      given = mutableMapOf(12 to "a", 15 to "b"),
      whenWe = { mutateValues { k, v -> "$k -> $v" } },
      then = { it shouldContainExactly mapOf(12 to "12 -> a", 15 to "15 -> b") },
    )

  @Test
  fun `removes null mutations`() =
    testMutation(
      given = mutableMapOf(12 to "a", 15 to "b", 10 to "c"),
      whenWe = { mutateValuesNotNull { (k, v) -> "$k -> $v".takeUnless { v == "b" } } },
      then = { it shouldContainExactly mapOf(12 to "12 -> a", 10 to "10 -> c") },
    )

  @Test
  fun `removes null mutations deconstructed`() =
    testMutation(
      given = mutableMapOf(12 to "a", 15 to "b", 10 to "c"),
      whenWe = { mutateValuesNotNull { k, v -> "$k -> $v".takeUnless { v == "b" } } },
      then = { it shouldContainExactly mapOf(12 to "12 -> a", 10 to "10 -> c") },
    )

  @Test
  fun `keep values of a given type`() =
    testMutation<MutableMap<Int, Any>>(
      given = mutableMapOf(12 to "a", 15 to 5, 10 to "c"),
      whenWe =
        assumingCast {
          removeValuesUnlessInstanceOf<_, String>().mutateValues { (k, v) -> "$k -> $v" }
        },
      then = { it shouldContainExactly mapOf(12 to "12 -> a", 10 to "10 -> c") },
    )
}
