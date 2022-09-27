package at.kopyk

import kotlin.test.assertContentEquals
import org.junit.jupiter.api.Test

class MutableListTest {

  @Test
  fun `can mutate all items`() {
    val list = mutableListOf("a", "b", "c")

    val result = list.mutateAll { it + it }

    assertContentEquals(actual = result, expected = listOf("aa", "bb", "cc"))
  }

  @Test
  fun `can mutate all items indexed`() {
    val list = mutableListOf("a", "b", "c")

    val result = list.mutateAllIndexed { index, value -> value + index }

    assertContentEquals(actual = result, expected = listOf("a0", "b1", "c2"))
  }

}
