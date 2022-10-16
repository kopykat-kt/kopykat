package at.kopyk

import org.junit.jupiter.api.Test

class CopyConstructorsWithValueTest {

  private fun copyFrom(annotatedModifiers: String, targetModifiers: String) {
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
  }

  @Test
  fun `value class @CopyFrom data class`() = copyFrom("value", "data")

  @Test
  fun `value class @CopyFrom class`() = copyFrom("value", "")

  @Test
  fun `data class @CopyFrom value class`() = copyFrom("data", "value")

  @Test
  fun `class @CopyFrom value class`() = copyFrom("", "value")

  private fun copyTo(annotatedModifiers: String, targetModifiers: String) {
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
  }

  @Test
  fun `value class @CopyTo data class`() = copyTo("value", "data")

  @Test
  fun `value class @CopyTo class`() = copyTo("value", "")

  @Test
  fun `data class @CopyTo value class`() = copyTo("data", "value")

  @Test
  fun `class @CopyTo value class`() = copyTo("", "value")

  private fun copy(annotatedModifiers: String, targetModifiers: String) {
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
  }

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
      |class Person()
      |
      |@JvmInline
      |@Copy(Person::class)
      |value class LocalPerson(val name: String)
      |
      |val p1 = Person()
      |val p2 = LocalPerson(p1)
      |val r = p2.name
      """.failsWith { it.contains("LocalPerson must have the same constructor properties as Person") }
  }

  @Test
  fun `@CopyFrom, missing field should fail compilation`() {
    """
      |import at.kopyk.CopyFrom
      |
      |class Person()
      |
      |@JvmInline
      |@CopyFrom(Person::class)
      |value class LocalPerson(val name: String)
      |
      |val p1 = Person()
      |val p2 = LocalPerson(p1)
      |val r = p2.name
      """.failsWith { it.contains("LocalPerson must have the same constructor properties as Person") }
  }

  @Test
  fun `@CopyTo, missing field should fail compilation`() {
    """
      |import at.kopyk.CopyTo
      |
      |class Person()
      |
      |@JvmInline
      |@CopyTo(Person::class)
      |data class LocalPerson(val name: String)
      |
      |val p1 = Person()
      |val p2 = LocalPerson(p1)
      |val r = p2.age
      """.failsWith { it.contains("Person must have the same constructor properties as LocalPerson") }
  }
}
