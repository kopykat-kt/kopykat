package at.kopyk

import org.junit.jupiter.api.Test

class CopyConstructorsTest {

  private fun copyFrom(annotatedModifiers: String, targetModifiers: String) {
    if (annotatedModifiers == "value" || targetModifiers == "value") {
      """
      |import at.kopyk.CopyFrom
      |
      |${if (targetModifiers == "value") "@JvmInline" else ""}
      |$targetModifiers class Person(val name: String)
      |
      |@CopyFrom(Person::class)
      |${if (annotatedModifiers == "value") "@JvmInline" else ""}
      |$annotatedModifiers class LocalPerson(val name: String)
      |
      |val p1 = Person("Alex")
      |val p2 = LocalPerson(p1)
      |val r = p2.name
      """.evals("r" to "Alex")
    } else {
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
  }

  @Test
  fun `class @CopyFrom class`() = copyFrom("", "")

  @Test
  fun `data class @CopyFrom data class`() = copyFrom("data", "data")

  @Test
  fun `class @CopyFrom data class`() = copyFrom("", "data")

  @Test
  fun `data class @CopyFrom class`() = copyFrom("data", "")

  @Test
  fun `value class @CopyFrom data class`() = copyFrom("value", "data")

  @Test
  fun `value class @CopyFrom class`() = copyFrom("value", "")

  @Test
  fun `data class @CopyFrom value class`() = copyFrom("data", "value")

  @Test
  fun `class @CopyFrom value class`() = copyFrom("", "value")

  private fun copyTo(annotatedModifiers: String, targetModifiers: String) {
    if (annotatedModifiers == "value" || targetModifiers == "value") {
      """
      |import at.kopyk.CopyTo
      |
      |${if (targetModifiers == "value") "@JvmInline" else ""}
      |$targetModifiers class Person(val name: String)
      |
      |@CopyTo(Person::class)
      |${if (annotatedModifiers == "value") "@JvmInline" else ""}
      |$annotatedModifiers class LocalPerson(val name: String)
      |
      |val p1 = LocalPerson("Alex")
      |val p2 = Person(p1)
      |val r = p2.name
      """.evals("r" to "Alex")
    } else {
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
  }

  @Test
  fun `class @CopyTo class`() = copyTo("", "")

  @Test
  fun `data class @CopyTo data class`() = copyTo("data", "data")

  @Test
  fun `class @CopyTo data class`() = copyTo("", "data")

  @Test
  fun `data class @CopyTo class`() = copyTo("data", "")

  @Test
  fun `value class @CopyTo data class`() = copyTo("value", "data")

  @Test
  fun `value class @CopyTo class`() = copyTo("value", "")

  @Test
  fun `data class @CopyTo value class`() = copyTo("data", "value")

  @Test
  fun `class @CopyTo value class`() = copyTo("", "value")

  private fun copy(annotatedModifiers: String, targetModifiers: String) {
    if (annotatedModifiers == "value" || targetModifiers == "value") {
      """
      |import at.kopyk.Copy
      |
      |${if (targetModifiers == "value") "@JvmInline" else ""}
      |$targetModifiers class Person(val name: String)
      |
      |@Copy(Person::class)
      |${if (annotatedModifiers == "value") "@JvmInline" else ""}
      |$annotatedModifiers class LocalPerson(val name: String)
      |
      |val p1 = LocalPerson("Alex")
      |val p2 = Person(p1)
      |val p3 = LocalPerson(p2)
      |val r = p3.name
      """.evals("r" to "Alex")
    } else {
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
  fun `value class @Copy data class`() = copy("value", "data")

  @Test
  fun `value class @Copy class`() = copy("value", "")

  @Test
  fun `data class @Copy value class`() = copy("data", "value")

  @Test
  fun `class @Copy value class`() = copy("", "value")

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
