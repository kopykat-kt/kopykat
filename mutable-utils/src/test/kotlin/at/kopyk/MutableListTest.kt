package at.kopyk

import io.kotest.matchers.collections.shouldContainExactly
import org.junit.jupiter.api.Test

class MutableListTest {

  @Test
  fun `can mutate all items`() {
    val list = mutableListOf("a", "b", "c")

    val result = list.mutateAll { it + it }

    result shouldContainExactly listOf("aa", "bb", "cc")
  }

  @Test
  fun `can mutate all items indexed`() {
    val list = mutableListOf("a", "b", "c")

    val result = list.mutateAllIndexed { index, value -> value + index }

    result shouldContainExactly listOf("a0", "b1", "c2")
  }

  @Test
  fun `removes null transformations`() {
    val list = mutableListOf("a", "b", "c")

    val result = list.mutateAllNotNull { value -> value.takeUnless { it == "b" } }

    result shouldContainExactly listOf("a", "c")
  }

  @Test
  fun `removes null transformations with index`() {
    val list = mutableListOf("a", "b", "c")

    val result = list.mutateAllNotNullIndexed { index, value ->
      value.takeUnless { it == "b" }?.plus("$index")
    }

    result shouldContainExactly listOf("a0", "c2")
  }

}
