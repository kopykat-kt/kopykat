package at.kopyk

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class CopyConstructorsTest {

  companion object {
    @JvmStatic
    fun cases(): Stream<Arguments> = Stream.of(
      Arguments.of("", ""),
      Arguments.of("", "data"),
      Arguments.of("data", ""),
      Arguments.of("data", "data"),
    )
  }

  @ParameterizedTest(name = "{0} class CopyFrom {1} class")
  @MethodSource("cases")
  fun copyFrom(annotatedModifiers: String, targetModifiers: String) {
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

  @ParameterizedTest(name = "{0} class CopyTo {1} class")
  @MethodSource("cases")
  fun copyTo(annotatedModifiers: String, targetModifiers: String) {
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

  @ParameterizedTest(name = "{0} class Copy {1} class")
  @MethodSource("cases")
  fun copy(annotatedModifiers: String, targetModifiers: String) {
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

  @Test
  fun `@Copy duplicated annotation`() {
    """
    |import at.kopyk.Copy
    |
    |@Copy(LocalPerson::class)
    |data class Person(val name: String, val age: Int)
    |
    |@Copy(Person::class)
    |data class LocalPerson(val name: String, val age: Int)
    |
    |val p1 = Person("Alex", 1)
    |val p2 = LocalPerson(p1)
    |val r = p2.age
    """.evals("r" to 1)
  }

  @Test
  fun `nested copy constructors`() {
    """
    |import at.kopyk.Copy
    |
    |data class Person(val name: String, val job: Job)
    |
    |data class Job(val title: String, val department: Department)
    |
    |@JvmInline
    |value class Department(val name: String)
    |
    |@Copy(Person::class)
    |data class LocalPerson(val name: String, val job: LocalJob)
    |
    |@Copy(Job::class)
    |data class LocalJob(val title: String, val department: LocalDepartment)
    |
    |@Copy(Department::class)
    |@JvmInline
    |value class LocalDepartment(val name: String)
    |
    |val p1 = LocalPerson("Alex", LocalJob("Developer", LocalDepartment("Engineering")))
    |val p2 = Person(p1)
    |val n = p2.name
    |val t = p2.job.title
    |val d = p2.job.department.name
    """.evals(
      "n" to "Alex",
      "t" to "Developer",
      "d" to "Engineering"
    )
  }


  @Test
  fun `multiple constructors`() {
    """
    |import at.kopyk.Copy
    |
    |@Copy(LocalPerson::class)
    |@Copy(RemotePerson::class)
    |data class Person(val name: String, val age: Int)
    |
    |data class LocalPerson(val name: String, val age: Int)
    |
    |data class RemotePerson(val name: String, val age: Int)
    |
    |val p1 = LocalPerson("Alex", 1)
    |val p2 = Person(p1)
    |val p3 = RemotePerson(p2)
    |val a = p3.age
    """.evals("a" to 1)
  }
}
