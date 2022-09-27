package at.kopyk

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.junit.jupiter.api.Test

class MutableListTest {

  @Test
  fun `can mutate all items`() {
    val original = mutableListOf("a", "b", "c")

    val result = original.mutateAll { it + it }

    original shouldContainExactly listOf("aa", "bb", "cc")
    result shouldContainExactly listOf("aa", "bb", "cc")
    original shouldBeSameInstanceAs result
  }

  @Test
  fun `can mutate all items indexed`() {
    val original = mutableListOf("a", "b", "c")

    val result = original.mutateAllIndexed { index, value -> value + index }

    original shouldContainExactly listOf("a0", "b1", "c2")
    result shouldContainExactly listOf("a0", "b1", "c2")
    original shouldBeSameInstanceAs result
  }

  @Test
  fun `removes null mutations`() {
    val original = mutableListOf("a", "b", "c")

    val result = original.mutateAllNotNull { value -> value.takeUnless { it == "b" } }

    original shouldContainExactly listOf("a", "c")
    result shouldContainExactly listOf("a", "c")
    original shouldBeSameInstanceAs result
  }

  @Test
  fun `removes null mutations with index`() {
    val original = mutableListOf("a", "b", "c")

    val result = original.mutateAllNotNullIndexed { index, value ->
      value.takeUnless { it == "b" }?.plus("$index")
    }

    original shouldContainExactly listOf("a0", "c2")
    result shouldContainExactly listOf("a0", "c2")
    original shouldBeSameInstanceAs result
  }

  @Test
  fun `keep instances of a given type`() {
    val original: MutableList<Any> = mutableListOf("a", 10, "c")

    val result: MutableList<String> = original.removeUnlessInstanceOf<String, _>().mutateAll { it + it }

    original shouldContainExactly listOf("aa", "cc")
    result shouldContainExactly listOf("aa", "cc")
    original shouldBeSameInstanceAs result
  }

}
