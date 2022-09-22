package fp.serrano.kopykat

import org.junit.jupiter.api.Test

class PackageTest {

  @Test
  fun `copyMap, simple test`() {
    """
      |package kopykat.tests
      |
      |data class Person(val name: String, val age: Int)
      |
      |val p1 = Person("Alex", 1)
      |val p2 = p1.copyMap(age = { it + 1 })
      |val r = p2.age
      """.evals("r" to 2)
  }

  @Test
  fun `copyMap, works on generic classes`() {
    """
      |package kopykat.tests
      |
      |data class Person<A>(val name: String, val age: A)
      |
      |val p1: Person<Int> = Person("Alex", 1)
      |val p2 = p1.copyMap(age = { it + 1 })
      |val r = p2.age
      """.evals("r" to 2)
  }

  @Test
  fun `mutate one property`() {
    """
      |package kopykat.tests
      |
      |data class Person(val name: String, val age: Int)
      |
      |val p1 = Person("Alex", 1)
      |val p2 = p1.copy { age = age + 1 }
      |val r = p2.age
      """.evals("r" to 2)
  }

  @Test
  fun `access the old value`() {
    """
      |package kopykat.tests
      |
      |data class Person(val name: String, val age: Int)
      |
      |val p1 = Person("Alex", 1)
      |val p2 = p1.copy {
      |    age++
      |    age = old.age
      |  }
      |val r = p2.age
      """.evals("r" to 1)
  }

  @Test
  fun `value copy, simple test`() {
    """
      |package kopykat.tests
      |
      |@JvmInline
      |value class Age(val age: Int)
      |
      |val a1 = Age(1)
      |val a2 = a1.copy { age++ }
      |val r = a2.age
      """.evals("r" to 2)
  }
}
