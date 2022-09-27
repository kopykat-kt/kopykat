package at.kopyk

import org.junit.jupiter.api.Test

class CopyMapTest {

  @Test
  fun `simple test`() {
    """
      |data class Person(val name: String, val age: Int)
      |
      |val p1 = Person("Alex", 1)
      |val p2 = p1.copyMap(age = { it + 1 })
      |val r = p2.age
      """.evals("r" to 2)
  }

  @Test
  fun `simple test on nested class`() {
    """
      |class Things {
      |  data class Person(val name: String, val age: Int)
      |}
      |
      |val p1 = Things.Person("Alex", 1)
      |val p2 = p1.copyMap(age = { it + 1 })
      |val r = p2.age
      """.evals("r" to 2)
  }

  @Test
  fun `empty transform does nothing`() {
    """
      |data class Person(val name: String, val age: Int)
      |
      |val p1 = Person("Alex", 1)
      |val p2 = p1.copyMap()
      |val r = p2.age
      """.evals("r" to 1)
  }

  @Test
  fun `works on generic classes, 1`() {
    """
      |data class Person<A>(val name: String, val age: A)
      |
      |val p1: Person<Int> = Person("Alex", 1)
      |val p2 = p1.copyMap(age = { it + 1 })
      |val r = p2.age
      """.evals("r" to 2)
  }

  @Test
  fun `works on generic classes, 2`() {
    """
      |data class Person<A>(val name: String, val infos: List<A>)
      |
      |val p1: Person<Int> = Person("Alex", listOf(1, 2))
      |val p2 = p1.copyMap(infos = { it.map { i -> i + 1 } })
      |val r = p2.infos
      """.evals("r" to listOf(2, 3))
  }

  @Test
  fun `not generated for regular classes`() {
    """
      |class Person(val name: String, val age: Int)
      |
      |val p1 = Person("Alex", 1)
      |val p2 = p1.copyMap()
      |val r = p2.age
      """.failsWith { it.contains("Unresolved reference: copyMap") }
  }

  @Test
  fun `simple test with additional property`() {
    """
      |data class Person(val name: String, val age: Int) {
      |  public var address: String = ""
      |}
      |
      |val p1 = Person("Alex", 1)
      |val p2 = p1.copyMap(age = { it + 1 })
      |val r = p2.age
      """.evals("r" to 2)
  }

  @Test
  fun `generic classes and variance, issue #48`() {
    """
      |data class Node<out T>(
      |  val current: T,
      |  val paths: List<Node<T>>,
      |)
      |
      |val n1: Node<Int> = Node(1, paths = listOf(Node(2, emptyList())))
      |val n2 = n1.copyMap(current = { it + 1 })
      |val r = n2.current
      """.evals("r" to 2)
  }

  @Test
  fun `uses a different field`() {
    """
      |data class Person(val name: String, val age: Int)
      |
      |val p1 = Person("Alex", 1)
      |val p2 = p1.copyMap(age = { name.count() })
      |val r = p2.age
      """.evals("r" to 4)
  }
}
