package at.kopyk

import org.junit.jupiter.api.Test

class CopyConstructorsTest {

  private fun copyFrom(annotatedModifiers: String, targetModifiers: String) {
    """
    |import at.kopyk.CopyFrom
    |
    |$targetModifiers class Person(val name: String, val age: Int)
    |
    |@CopyFrom(Person::class)
    |$annotatedModifiers class LocalPerson(val name: String, val age: Int)
    |
    |val p1 = Person("Alex", 1)
    |val p2 = LocalPerson(p1)
    |val r = p2.age
    """.evals("r" to 1)
  }

  @Test
  fun `class @CopyFrom class`() = copyFrom("", "")

  @Test
  fun `data class @CopyFrom data class`() = copyFrom("data", "data")

  @Test
  fun `class @CopyFrom data class`() = copyFrom("", "data")

  @Test
  fun `data class @CopyFrom class`() = copyFrom("data", "")

  private fun copyTo(annotatedModifiers: String, targetModifiers: String) {
    """
    |import at.kopyk.CopyTo
    |
    |$targetModifiers class Person(val name: String, val age: Int)
    |
    |@CopyTo(Person::class)
    |$annotatedModifiers class LocalPerson(val name: String, val age: Int)
    |
    |val p1 = LocalPerson("Alex", 1)
    |val p2 = Person(p1)
    |val r = p2.age
    """.evals("r" to 1)
  }

  @Test
  fun `class @CopyTo class`() = copyTo("", "")

  @Test
  fun `data class @CopyTo data class`() = copyTo("data", "data")

  @Test
  fun `class @CopyTo data class`() = copyTo("", "data")

  @Test
  fun `data class @CopyTo class`() = copyTo("data", "")

  private fun copy(annotatedModifiers: String, targetModifiers: String) {
    """
    |import at.kopyk.Copy
    |
    |$targetModifiers class Person(val name: String, val age: Int)
    |
    |@Copy(Person::class)
    |$annotatedModifiers class LocalPerson(val name: String, val age: Int)
    |
    |val p1 = LocalPerson("Alex", 1)
    |val p2 = Person(p1)
    |val p3 = LocalPerson(p2)
    |val r = p3.age
    """.evals("r" to 1)
  }

  @Test
  fun `class @Copy class`() = copy("", "")

  @Test
  fun `data class @Copy data class`() = copy("data", "data")

  @Test
  fun `class @Copy data class`() = copy("", "data")

  @Test
  fun `data class @Copy class`() = copy("data", "")

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
