package at.kopyk

import org.junit.jupiter.api.Test

class CopyConstructorsTest {

  @Test
  fun `simple test`() {
    """
      |import at.kopyk.CopyFrom
      |
      |data class Person(val name: String, val age: Int)
      |
      |@CopyFrom(Person::class)
      |data class LocalPerson(val name: String, val age: Int)
      |
      |val p1 = Person("Alex", 1)
      |val p2 = LocalPerson(p1)
      |val r = p2.age
      """.evals("r" to 1)
  }

  @Test
  fun `simple test, non-data class`() {
    """
      |import at.kopyk.CopyFrom
      |
      |data class Person(val name: String, val age: Int)
      |
      |@CopyFrom(Person::class)
      |class LocalPerson(val name: String, val age: Int)
      |
      |val p1 = Person("Alex", 1)
      |val p2 = LocalPerson(p1)
      |val r = p2.age
      """.evals("r" to 1)
  }

  @Test
  fun `missing field should not create`() {
    """
      |import at.kopyk.CopyFrom
      |
      |data class Person(val name: String)
      |
      |@CopyFrom(Person::class)
      |data class LocalPerson(val name: String, val age: Int)
      |
      |val p1 = Person("Alex", 1)
      |val p2 = LocalPerson(p1)
      |val r = p2.age
      """.failsWith { it.contains("LocalPerson must have the same constructor properties as Person") }
  }
}
