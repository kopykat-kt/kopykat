package at.kopyk

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class CopyConstructorsWithValueTest {
  companion object {
    @JvmStatic
    fun cases(): Stream<Arguments> =
      Stream.of(
        Arguments.of("value", ""),
        Arguments.of("value", "data"),
        Arguments.of("value", "value"),
        Arguments.of("", "value"),
        Arguments.of("data", "value"),
      )
  }

  @ParameterizedTest(name = "{0} class CopyFrom {1} class")
  @MethodSource("cases")
  fun copyFrom(
    annotatedModifiers: String,
    targetModifiers: String,
  ) {
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

  @ParameterizedTest(name = "{0} class CopyTo {1} class")
  @MethodSource("cases")
  fun copyTo(
    annotatedModifiers: String,
    targetModifiers: String,
  ) {
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

  @ParameterizedTest(name = "{0} class Copy {1} class")
  @MethodSource("cases")
  fun copy(
    annotatedModifiers: String,
    targetModifiers: String,
  ) {
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
      """.failsWith {
      it.contains("LocalPerson must have the same constructor properties as Person")
    }
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
      """.failsWith {
      it.contains("LocalPerson must have the same constructor properties as Person")
    }
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
      """.failsWith {
      it.contains("Person must have the same constructor properties as LocalPerson")
    }
  }
}
