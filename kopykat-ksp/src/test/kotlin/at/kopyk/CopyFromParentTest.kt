package at.kopyk

import org.junit.jupiter.api.Test

class CopyFromParentTest {

  @Test
  fun `simple test`() {
    """
      |import at.kopyk.CopyFrom
      |
      |interface Person {
      |  val name: String
      |  val age: Int
      |}
      |
      |@CopyFrom(Person::class)
      |data class Person1(override val name: String, override val age: Int): Person
      |
      |@CopyFrom(Person::class)
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
      |import at.kopyk.CopyFrom
      |
      |interface Person {
      |  val name: String
      |  val age: Int
      |}
      |
      |@CopyFrom(Person::class)
      |class Person1(override val name: String, override val age: Int): Person
      |
      |@CopyFrom(Person::class)
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
      |import at.kopyk.CopyFrom
      |
      |interface Person {
      |  val name: String
      |}
      |
      |@CopyFrom(Person::class)
      |data class Person1(override val name: String, val age: Int): Person
      |
      |@CopyFrom(Person::class)
      |data class Person2(override val name: String, val age: Int): Person
      |
      |val p1 = Person1("Alex", 1)
      |val p2 = Person2(p1)
      |val r = p2.age
      """.failsWith {
      it.contains("Person1 must have the same constructor properties as Person")
      it.contains("Person2 must have the same constructor properties as Person")
    }
  }
}
