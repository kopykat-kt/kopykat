package at.kopyk

import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.junit.jupiter.api.Test

class MutableMapTest {

  @Test
  fun `can mutate all entries`() {
    val original = mutableMapOf(12 to "a", 15 to "b")

    val result = original.mutateValues { (k, v) -> "$k -> $v" }

    original shouldContainExactly mapOf(12 to "12 -> a", 15 to "15 -> b")
    result shouldContainExactly mapOf(12 to "12 -> a", 15 to "15 -> b")
    original shouldBeSameInstanceAs result
  }

  @Test
  fun `can mutate all items deconstructed`() {
    val original = mutableMapOf(12 to "a", 15 to "b")

    val result = original.mutateValues { k, v -> "$k -> $v" }

    original shouldContainExactly mapOf(12 to "12 -> a", 15 to "15 -> b")
    result shouldContainExactly mapOf(12 to "12 -> a", 15 to "15 -> b")
    original shouldBeSameInstanceAs result
  }

  @Test
  fun `removes null mutations`() {
    val original = mutableMapOf(12 to "a", 15 to "b", 10 to "c")

    val result = original.mutateValuesNotNull { (k, v) -> "$k -> $v".takeUnless { v == "b" } }

    original shouldContainExactly mapOf(12 to "12 -> a", 10 to "10 -> c")
    result shouldContainExactly mapOf(12 to "12 -> a", 10 to "10 -> c")
    original shouldBeSameInstanceAs result
  }

  @Test
  fun `removes null mutations deconstructed`() {
    val original = mutableMapOf(12 to "a", 15 to "b", 10 to "c")

    val result = original.mutateValuesNotNull { k, v -> "$k -> $v".takeUnless { v == "b" } }

    original shouldContainExactly mapOf(12 to "12 -> a", 10 to "10 -> c")
    result shouldContainExactly mapOf(12 to "12 -> a", 10 to "10 -> c")
    original shouldBeSameInstanceAs result
  }

  @Test
  fun `keep values of a given type`() {
    val original: MutableMap<Int, Any> = mutableMapOf(12 to "a", 15 to 5, 10 to "c")

    val result: MutableMap<Int, String> = original.removeValuesUnlessInstanceOf<String, _, _>()
      .mutateValues { (k, v) -> "$k -> $v" }

    original shouldContainExactly mapOf(12 to "12 -> a", 10 to "10 -> c")
    result shouldContainExactly mapOf(12 to "12 -> a", 10 to "10 -> c")
    original shouldBeSameInstanceAs result
  }

}
