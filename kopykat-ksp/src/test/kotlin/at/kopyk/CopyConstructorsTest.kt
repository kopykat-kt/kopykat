package at.kopyk

import org.junit.jupiter.api.Test

class CopyConstructorsTest {

  @Test
  fun `data class @CopyFrom data class`() {
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
  fun `class @CopyFrom data class`() {
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
  fun `data class @CopyFrom class`() {
    """
      |import at.kopyk.CopyFrom
      |
      |class Person(val name: String, val age: Int)
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
  fun `data class @CopyTo data class`() {
    """
      |import at.kopyk.CopyTo
      |
      |data class Person(val name: String, val age: Int)
      |
      |@CopyTo(Person::class)
      |data class LocalPerson(val name: String, val age: Int)
      |
      |val p1 = LocalPerson("Alex", 1)
      |val p2 = Person(p1)
      |val r = p2.age
      """.evals("r" to 1)
  }

  @Test
  fun `class @CopyTo data class`() {
    """
      |import at.kopyk.CopyTo
      |
      |data class Person(val name: String, val age: Int)
      |
      |@CopyTo(Person::class)
      |class LocalPerson(val name: String, val age: Int)
      |
      |val p1 = LocalPerson("Alex", 1)
      |val p2 = Person(p1)
      |val r = p2.age
      """.evals("r" to 1)
  }

  @Test
  fun `data class @CopyTo class`() {
    """
      |import at.kopyk.CopyTo
      |
      |class Person(val name: String, val age: Int)
      |
      |@CopyTo(Person::class)
      |data class LocalPerson(val name: String, val age: Int)
      |
      |val p1 = LocalPerson("Alex", 1)
      |val p2 = Person(p1)
      |val r = p2.age
      """.evals("r" to 1)
  }

  @Test
  fun `data class @Copy data class`() {
    """
      |import at.kopyk.Copy
      |
      |data class Person(val name: String, val age: Int)
      |
      |@Copy(Person::class)
      |data class LocalPerson(val name: String, val age: Int)
      |
      |val p1 = LocalPerson("Alex", 1)
      |val p2 = Person(p1)
      |val p3 = LocalPerson(p2)
      |val r = p3.age
      """.evals("r" to 1)
  }

  @Test
  fun `class @Copy data class`() {
    """
      |import at.kopyk.Copy
      |
      |data class Person(val name: String, val age: Int)
      |
      |@Copy(Person::class)
      |class LocalPerson(val name: String, val age: Int)
      |
      |val p1 = LocalPerson("Alex", 1)
      |val p2 = Person(p1)
      |val p3 = LocalPerson(p2)
      |val r = p3.age
      """.evals("r" to 1)
  }

  @Test
  fun `data class @Copy class`() {
    """
      |import at.kopyk.Copy
      |
      |class Person(val name: String, val age: Int)
      |
      |@Copy(Person::class)
      |data class LocalPerson(val name: String, val age: Int)
      |
      |val p1 = LocalPerson("Alex", 1)
      |val p2 = Person(p1)
      |val p3 = LocalPerson(p2)
      |val r = p3.age
      """.evals("r" to 1)
  }

  @Test
  fun `@Copy, missing field should fail compilation`() {
    """
      |import at.kopyk.Copy
      |
      |data class Person(val name: String)
      |
      |@Copy(Person::class)
      |data class LocalPerson(val name: String, val age: Int)
      |
      |val p1 = Person("Alex", 1)
      |val p2 = LocalPerson(p1)
      |val r = p2.age
      """.failsWith { it.contains("LocalPerson must have the same constructor properties as Person") }
  }

  @Test
  fun `@CopyFrom, missing field should fail compilation`() {
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

  @Test
  fun `@CopyTo, missing field should fail compilation`() {
    """
      |import at.kopyk.CopyTo
      |
      |data class Person(val name: String)
      |
      |@CopyTo(Person::class)
      |data class LocalPerson(val name: String, val age: Int)
      |
      |val p1 = Person("Alex", 1)
      |val p2 = LocalPerson(p1)
      |val r = p2.age
      """.failsWith { it.contains("Person must have the same constructor properties as LocalPerson") }
  }
}
