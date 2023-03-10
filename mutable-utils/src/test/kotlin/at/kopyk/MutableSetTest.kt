package at.kopyk

import at.kopyk.utils.assumingCast
import io.kotest.matchers.collections.shouldContainExactly
import org.junit.jupiter.api.Test

class MutableSetTest {

  @Test
  fun `can mutate all items`() = testMutation(
    given = mutableSetOf("a", "b", "c"),
    whenWe = { mutateAll { it + it } },
    then = { it shouldContainExactly listOf("aa", "bb", "cc") }
  )

  @Test
  fun `can mutate all items indexed`() = testMutation(
    given = mutableSetOf("a", "b", "c"),
    whenWe = { mutateAllIndexed { index, value -> value + index } },
    then = { it shouldContainExactly listOf("a0", "b1", "c2") }
  )

  @Test
  fun `removes null mutations`() = testMutation(
    given = mutableSetOf("a", "b", "c"),
    whenWe = { mutateAllNotNull { value -> value.takeUnless { it == "b" } } },
    then = { it shouldContainExactly listOf("a", "c") }
  )

  @Test
  fun `removes null mutations with index`() = testMutation(
    given = mutableSetOf("a", "b", "c"),
    whenWe = { mutateAllIndexedNotNull { i, v -> v.takeUnless { it == "b" }?.plus("$i") } },
    then = { it shouldContainExactly listOf("a0", "c2") }
  )

  @Test
  fun `keep instances of a given type`() = testMutation(
    given = mutableSetOf("a", 10, "c"),
    whenWe = assumingCast { removeUnlessInstanceOf<String>().mutateAll { it + it } },
    then = { it shouldContainExactly listOf("aa", "cc") }
  )
}
