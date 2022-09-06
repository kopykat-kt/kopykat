package fp.serrano.transformative

import org.junit.jupiter.api.Test

class MutableCopyTest {

  @Test
  fun `simple test`() {
    """
      |import fp.serrano.MutableCopy
      |
      |@MutableCopy data class Person(val name: String, val age: Int)
      |
      |val p1 = Person("Alex", 1)
      |val p2 = p1.copy { age = age + 1 }
      |val r = p2.age
      """.evals("r" to 2)
  }

  @Test
  fun `complex test`() {
    """
      |import fp.serrano.MutableCopy
      |
      |@MutableCopy data class Person(val name: String, val age: Int)
      |
      |val p1 = Person("Alex", 1)
      |val p2 = p1.copy { 
      |  name = name + " Serrano"
      |  age = age + 1 
      |}
      |val r = p2.age
      |val n = p2.name
      """.evals("r" to 2, "n" to "Alex Serrano")
  }

  @Test
  fun `fails on non-data class`() {
    """
      |import fp.serrano.MutableCopy
      |
      |@MutableCopy class Person(val name: String, val age: Int)
      """.failsWith { it.contains("Only data classes can be annotated with @MutableCopy") }
  }

  @Test
  fun `empty transform does nothing`() {
    """
      |import fp.serrano.MutableCopy
      |
      |@MutableCopy data class Person(val name: String, val age: Int)
      |
      |val p1 = Person("Alex", 1)
      |val p2 = p1.copy { }
      |val r = p2.age
      """.evals("r" to 1)
  }

  @Test
  fun `works on generic classes`() {
    """
      |import fp.serrano.MutableCopy
      |
      |@MutableCopy data class Person<A>(val name: String, val age: A)
      |
      |val p1: Person<Int> = Person("Alex", 1)
      |val p2 = p1.copy { age = age + 1 }
      |val r = p2.age
      """.evals("r" to 2)
  }
}
