package at.kopyk

import org.junit.jupiter.api.Test

class AnnotationTest {

  @Test
  fun `generates with annotation`() {
    """
      |import at.kopyk.KopyKat
      |
      |@KopyKat
      |data class Person(val name: String, val age: Int)
      |
      |val p1 = Person("Alex", 1)
      |val p2 = p1.copyMap(age = { it + 1 })
      |val r = p2.age
      """.evalsWithArgs(mapOf("annotatedOnly" to "true"),"r" to 2)
  }

  @Test
  fun `doesn't generate without annotation`() {
    """
      |data class Person(val name: String, val age: Int)
      |
      |val p1 = Person("Alex", 1)
      |val p2 = p1.copyMap(age = { it + 1 })
      |val r = p2.age
      """.failsWith(mapOf("annotatedOnly" to "true")) { it.contains("Unresolved reference: copyMap") }
  }

  @Test
  fun `warning for unused annotation`() {
    """
      |import at.kopyk.KopyKat
      |
      |@KopyKat
      |data class Person(val name: String, val age: Int)
      |
      |val p1 = Person("Alex", 1)
      |val p2 = p1.copyMap(age = { it + 1 })
      |val r = p2.age
      """.compilesWith(mapOf("annotatedOnly" to "false")) {
        it.contains("Unused '@KopyKat' annotation")
      }
  }
}
