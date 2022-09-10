package fp.serrano.transformative

import org.junit.jupiter.api.Test

class ValueCopyTest {

  @Test
  fun `simple test`() {
    """
      |@JvmInline
      |value class Age(val age: Int)
      |
      |val a1 = Age(1)
      |val a2 = a1.copy { it + 1 }
      |val r = a2.age
      """.evals("r" to 2)
  }

  @Test
  fun `empty transform does nothing`() {
    """
      |@JvmInline
      |value class Age(val age: Int)
      |
      |val a1 = Age(1)
      |val a2 = a1.copy()
      |val r = a2.age
      """.evals("r" to 1)
  }
}
