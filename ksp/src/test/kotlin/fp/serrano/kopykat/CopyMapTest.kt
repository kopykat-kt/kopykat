package fp.serrano.kopykat

import org.junit.jupiter.api.Test

class CopyMapTest {

  @Test
  fun `simple test`() {
    """
      |data class Person(val name: String, val age: Int)
      |
      |val p1 = Person("Alex", 1)
      |val p2 = p1.copyMap(age = { it + 1 })
      |val r = p2.age
      """.evals("r" to 2)
  }

  @Test
  fun `empty transform does nothing`() {
    """
      |data class Person(val name: String, val age: Int)
      |
      |val p1 = Person("Alex", 1)
      |val p2 = p1.copyMap()
      |val r = p2.age
      """.evals("r" to 1)
  }

  @Test
  fun `works on generic classes`() {
    """
      |data class Person<A>(val name: String, val age: A)
      |
      |val p1: Person<Int> = Person("Alex", 1)
      |val p2 = p1.copyMap(age = { it + 1 })
      |val r = p2.age
      """.evals("r" to 2)
  }

  @Test
  fun `not generated for regular classes`() {
    """
      |class Person(val name: String, val age: Int)
      |
      |val p1 = Person("Alex", 1)
      |val p2 = p1.copyMap()
      |val r = p2.age
      """.failsWith { it.contains("Unresolved reference: copyMap") }
  }
}
