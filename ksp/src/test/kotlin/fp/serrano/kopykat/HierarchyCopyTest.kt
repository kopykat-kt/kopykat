package fp.serrano.kopykat

import org.junit.jupiter.api.Test

class HierarchyCopyTest {
  @Test
  fun `open property`() {
    """
      |sealed abstract class User(open val name: String)
      |data class Person(override val name: String, val age: Int): User(name)
      |data class Company(override val name: String, val address: String): User(name)
      |
      |val p1: User = Person("Alex", 1)
      |val p2 = p1.copy(name = "Pepe")
      |val r = (p2 as Person).name
      """.evals("r" to "Pepe")
  }

  @Test
  fun `open property, nested`() {
    """
      |sealed abstract class User(open val name: String) {
      |  data class Person(override val name: String, val age: Int): User(name)
      |  data class Company(override val name: String, val address: String): User(name)
      |}
      |
      |val p1: User = User.Person("Alex", 1)
      |val p2 = p1.copy(name = "Pepe")
      |val r = (p2 as User.Person).name
      """.evals("r" to "Pepe")
  }

  @Test
  fun `abstract property`() {
    """
      |sealed abstract class User {
      |  abstract val name: String
      |  abstract val other: Int
      |}
      |data class Person(override val name: String, val age: Int): User() {
      |  override val other = 1
      |}
      |data class Company(override val name: String, val address: String): User() {
      |  override val other = 2
      |}
      |
      |val p1: User = Person("Alex", 1)
      |val p2 = p1.copy(name = "Pepe")
      |val r = (p2 as Person).name
      """.evals("r" to "Pepe")
  }

  @Test
  fun `interface`() {
    """
      |sealed interface User {
      |  val name: String
      |}
      |data class Person(override val name: String, val age: Int): User
      |data class Company(override val name: String, val address: String): User
      |
      |val p1: User = Person("Alex", 1)
      |val p2 = p1.copy(name = "Pepe")
      |val r = (p2 as Person).name
      """.evals("r" to "Pepe")
  }

  @Test
  fun `open property, mutable`() {
    """
      |sealed abstract class User(open val name: String)
      |data class Person(override val name: String, val age: Int): User(name)
      |data class Company(override val name: String, val address: String): User(name)
      |
      |val p1: User = Person("Alex", 1)
      |val p2 = p1.copy { name = "Pepe" }
      |val r = (p2 as Person).name
      """.evals("r" to "Pepe")
  }

  @Test
  fun `open property, mapping`() {
    """
      |sealed abstract class User(open val name: String)
      |data class Person(override val name: String, val age: Int): User(name)
      |data class Company(override val name: String, val address: String): User(name)
      |
      |val p1: User = Person("Alex", 1)
      |val p2 = p1.copyMap(name = { it.lowercase() })
      |val r = (p2 as Person).name
      """.evals("r" to "alex")
  }

  @Test
  fun `field which is not everywhere`() {
    """
      |sealed abstract class User(open val name: String)
      |data class Person(override val name: String, val age: Int): User(name)
      |data class Company(override val name: String, val address: String): User(name)
      |
      |val p1: User = Person("Alex", 1)
      |val p2 = p1.copy(age = 2)
      """.failsWith { it.contains("Cannot find a parameter with this name: age") }
  }
}
