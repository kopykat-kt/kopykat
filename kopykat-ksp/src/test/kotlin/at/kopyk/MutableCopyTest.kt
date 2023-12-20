package at.kopyk

import org.junit.jupiter.api.Test

class MutableCopyTest {
  @Test
  fun `mutate one property`() {
    """
      |data class Person(val name: String, val age: Int)
      |
      |val p1 = Person("Alex", 1)
      |val p2 = p1.copy { age = age + 1 }
      |val r = p2.age
      """.evals("r" to 2)
  }

  @Test
  fun `mutate one property, internal`() {
    """
      |internal data class Person(val name: String, val age: Int)
      |
      |internal val p1 = Person("Alex", 1)
      |internal val p2 = p1.copy { age = age + 1 }
      |val r = p2.age
      """.evals("r" to 2)
  }

  @Test
  fun `weird package name, issue #78`() {
    """
      |package `this`.`in`.other
      |
      |data class Person(val name: String, val age: Int)
      |
      |val p1 = Person("Alex", 1)
      |val p2 = p1.copy { age = age + 1 }
      |val r = p2.age
      """.evals("r" to 2)
  }

  @Test
  fun `mutate one property, nested class`() {
    """
      |class Things {
      |  data class Person(val name: String, val age: Int)
      |}
      |
      |val p1 = Things.Person("Alex", 1)
      |val p2 = p1.copy { age = age + 1 }
      |val r = p2.age
      """.evals("r" to 2)
  }

  @Test
  fun `mutate one property, nullable type`() {
    """
      |data class Person(val name: String, val age: Int?)
      |
      |val p1 = Person("Alex", 1)
      |val p2 = p1.copy { age = age?.let { it + 1 } }
      |val r = p2.age
      """.evals("r" to 2)
  }

  @Test
  fun `mutate one property, nullable type, set to null`() {
    """
      |data class Person(val name: String, val age: Int?)
      |
      |val p1 = Person("Alex", 1)
      |val p2 = p1.copy { age = null }
      |val r = p2.age
      """.evals("r" to null)
  }

  @Test
  fun `mutate two properties`() {
    """
      |data class Person(val name: String, val age: Int)
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
  fun `mutate two properties (advanced)`() {
    """
      |data class Person(val name: String, val age: Int)
      |
      |val p1 = Person("Alex", 1)
      |val p2 = p1.copy { 
      |  name = "${"\$"}name Serrano"
      |  age++ 
      |}
      |val r = p2.age
      |val n = p2.name
      """.evals("r" to 2, "n" to "Alex Serrano")
  }

  @Test
  fun `empty transform does nothing`() {
    """
      |data class Person(val name: String, val age: Int)
      |
      |val p1 = Person("Alex", 1)
      |val p2 = p1.copy { }
      |val r = p2.age
      """.evals("r" to 1)
  }

  @Test
  fun `access the old value`() {
    """
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
  fun `works on generic classes`() {
    """
      |data class Person<A>(val name: String, val age: A)
      |
      |val p1: Person<Int> = Person("Alex", 1)
      |val p2 = p1.copy { age = age + 1 }
      |val r = p2.age
      """.evals("r" to 2)
  }

  @Test
  fun `each for lists`() {
    """
      |data class Person<A>(val name: String, val age: Int, val anns: List<A>)
      |
      |val p1: Person<Int> = Person("Alex", 1, listOf(1, 10))
      |val p2 = p1.copy { anns.forEachIndexed { i, v -> anns[i] = v + 1 } }
      |val r = p2.anns
      """.evals("r" to listOf(2, 11))
  }

  @Test
  fun `each for maps`() {
    """
      |data class Person(val name: String, val age: Int, val things: Map<String, Int>)
      |
      |val p1: Person = Person("Alex", 1, mapOf("chair" to 1, "pencil" to 10))
      |val p2 = p1.copy { things.forEach { (k, v) -> things[k] = v + 1 } }
      |val r = p2.things
      """.evals("r" to mapOf("chair" to 2, "pencil" to 11))
  }

  @Test
  fun `typealias test`() {
    """
      |import at.kopyk.CopyExtensions
      |
      |@CopyExtensions
      |typealias Person = Pair<String, Int>
      |
      |val p1 = "Alex" to 1
      |val p2 = p1.copy { second++ }
      |val r = p2.second
      """.evals("r" to 2)
  }

  @Test
  fun `typealias test, full generic`() {
    """
      |import at.kopyk.CopyExtensions
      |
      |@CopyExtensions
      |typealias Pareja<A, B> = Pair<A, B>
      |
      |val p1: Pareja<String, Int> = "Alex" to 1
      |val p2 = p1.copy { second++ }
      |val r = p2.second
      """.evals("r" to 2)
  }

  @Test
  fun `typealias test, half generic`() {
    """
      |import at.kopyk.CopyExtensions
      |
      |@CopyExtensions
      |typealias Named<A> = Pair<String, A>
      |
      |val p1: Named<Int> = "Alex" to 1
      |val p2 = p1.copy { second++ }
      |val r = p2.second
      """.evals("r" to 2)
  }

  @Test
  fun `issue #83, check null`() {
    """
      |data class Person(val d: List<String>? = null)
      |
      |val p = Person(listOf("e"))
      |val p2 = p.copy { d = null }
      |val r = p2.d
      """.evals("r" to null)
  }

  @Test
  fun `issue #83, check add`() {
    """
      |data class Person(val d: List<String>? = null)
      |
      |val p = Person(listOf("e"))
      |val p2 = p.copy { d?.add("f") ; Unit }
      |val r = p2.d
      """.evals("r" to listOf("e", "f"))
  }
}
