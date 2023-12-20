package at.kopyk

import org.junit.jupiter.api.Test

class ValueCopyMapTest {
  @Test
  fun `simple test`() {
    """
      |@JvmInline
      |value class Age(val age: Int)
      |
      |val a1 = Age(1)
      |val a2 = a1.copyMap { it + 1 }
      |val r = a2.age
      """.evals("r" to 2)
  }

  @Test
  fun `empty copyMap does nothing`() {
    """
      |@JvmInline
      |value class Age(val age: Int)
      |
      |val a1 = Age(1)
      |val a2 = a1.copyMap()
      |val r = a2.age
      """.evals("r" to 1)
  }
}
