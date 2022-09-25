package at.kopyk

import org.junit.jupiter.api.Test

class ValueCopyTest {

  @Test
  fun `simple test`() {
    """
      |@JvmInline
      |value class Age(val age: Int)
      |
      |val a1 = Age(1)
      |val a2 = a1.copy { age++ }
      |val r = a2.age
      """.evals("r" to 2)
  }

  @Test
  fun `empty copy does nothing`() {
    """
      |@JvmInline
      |value class Age(val age: Int)
      |
      |val a1 = Age(1)
      |val a2 = a1.copy { }
      |val r = a2.age
      """.evals("r" to 1)
  }
}
