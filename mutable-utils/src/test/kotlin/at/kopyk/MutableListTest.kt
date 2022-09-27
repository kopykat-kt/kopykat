package at.kopyk

import io.kotest.matchers.collections.shouldContainExactly
import org.junit.jupiter.api.Test

class MutableListTest {

  @Test
  fun `can mutate all items`() {
    val original = mutableListOf("a", "b", "c")

    val result = original.mutateAll { it + it }

    original shouldContainExactly listOf("aa", "bb", "cc")
    result shouldContainExactly listOf("aa", "bb", "cc")
  }

  @Test
  fun `can mutate all items indexed`() {
    val original = mutableListOf("a", "b", "c")

    val result = original.mutateAllIndexed { index, value -> value + index }

    original shouldContainExactly listOf("a0", "b1", "c2")
    result shouldContainExactly listOf("a0", "b1", "c2")
  }

  @Test
  fun `removes null mutations`() {
    val original = mutableListOf("a", "b", "c")

    val result = original.mutateAllNotNull { value -> value.takeUnless { it == "b" } }

    original shouldContainExactly listOf("a", "c")
    result shouldContainExactly listOf("a", "c")
  }

  @Test
  fun `removes null mutations with index`() {
    val original = mutableListOf("a", "b", "c")

    val result = original.mutateAllNotNullIndexed { index, value ->
      value.takeUnless { it == "b" }?.plus("$index")
    }

    original shouldContainExactly listOf("a0", "c2")
    result shouldContainExactly listOf("a0", "c2")
  }

}
