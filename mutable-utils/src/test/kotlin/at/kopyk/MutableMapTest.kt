package at.kopyk

import io.kotest.matchers.maps.shouldContainExactly
import org.junit.jupiter.api.Test

class MutableMapTest {

  @Test
  fun `can mutate all entries`() {
    val list = mutableMapOf(12 to "a", 15 to "b")

    val result = list.mutateValues { (k, v) -> "$k -> $v" }

    result shouldContainExactly mapOf(12 to "12 -> a", 15 to "15 -> b")
  }

  @Test
  fun `can mutate all items deconstructed`() {
    val list = mutableMapOf(12 to "a", 15 to "b")

    val result = list.mutateValues { k, v -> "$k -> $v" }

    result shouldContainExactly mapOf(12 to "12 -> a", 15 to "15 -> b")
  }

  @Test
  fun `removes null mutations`() {
    val list = mutableMapOf(12 to "a", 15 to "b", 10 to "c")

    val result = list.mutateValuesNotNull { (k, v) -> "$k -> $v".takeUnless { v == "b" } }

    result shouldContainExactly mapOf(12 to "12 -> a", 10 to "10 -> c")
  }

  @Test
  fun `removes null mutations deconstructed`() {
    val list = mutableMapOf(12 to "a", 15 to "b", 10 to "c")

    val result = list.mutateValuesNotNull { k, v -> "$k -> $v".takeUnless { v == "b" } }

    result shouldContainExactly mapOf(12 to "12 -> a", 10 to "10 -> c")
  }

}
