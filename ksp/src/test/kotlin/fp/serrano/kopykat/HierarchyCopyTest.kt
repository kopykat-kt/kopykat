package fp.serrano.kopykat

import org.junit.jupiter.api.Test

class HierarchyCopyTest {
  @Test
  fun `execute on one element`() {
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
  fun `mutable on one element`() {
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
  fun `execute map on one element`() {
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
