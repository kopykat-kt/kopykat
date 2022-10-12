package at.kopyk

import org.junit.jupiter.api.Test

class CopyFromParentTest {

  @Test
  fun `simple test`() {
    """
      |interface Person {
      |  val name: String
      |  val age: Int
      |}
      |data class Person1(override val name: String, override val age: Int): Person
      |data class Person2(override val name: String, override val age: Int): Person
      |
      |val p1 = Person1("Alex", 1)
      |val p2 = Person2(p1)
      |val r = p2.age
      """.evals("r" to 1)
  }

  @Test
  fun `simple test, non-data class`() {
    """
      |interface Person {
      |  val name: String
      |  val age: Int
      |}
      |class Person1(override val name: String, override val age: Int): Person
      |data class Person2(override val name: String, override val age: Int): Person
      |
      |val p1 = Person1("Alex", 1)
      |val p2 = Person2(p1)
      |val r = p2.age
      """.evals("r" to 1)
  }

  @Test
  fun `missing field should not create`() {
    """
      |interface Person {
      |  val name: String
      |}
      |data class Person1(override val name: String, val age: Int): Person
      |data class Person2(override val name: String, val age: Int): Person
      |
      |val p1 = Person1("Alex", 1)
      |val p2 = Person2(p1)
      |val r = p2.age
      """.failsWith { it.contains("No value passed for parameter") }
  }
}
