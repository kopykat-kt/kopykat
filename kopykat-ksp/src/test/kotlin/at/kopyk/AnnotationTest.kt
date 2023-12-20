package at.kopyk

import org.junit.jupiter.api.Test

class AnnotationTest {
  @Test
  fun `generates with annotation`() {
    """
      |import at.kopyk.CopyExtensions
      |
      |@CopyExtensions
      |data class Person(val name: String, val age: Int)
      |
      |val p1 = Person("Alex", 1)
      |val p2 = p1.copyMap(age = { it + 1 })
      |val r = p2.age
      """.evalsWithArgs(mapOf("generate" to KopyKatGenerate.ANNOTATED), "r" to 2)
  }

  @Test
  fun `doesn't generate without annotation`() {
    """
      |data class Person(val name: String, val age: Int)
      |
      |val p1 = Person("Alex", 1)
      |val p2 = p1.copyMap(age = { it + 1 })
      |val r = p2.age
      """.failsWith(mapOf("generate" to KopyKatGenerate.ANNOTATED)) {
      it.contains("Unresolved reference: copyMap")
    }
  }

  @Test
  fun `warning for unused annotation, implicit generateAll`() {
    """
      |import at.kopyk.CopyExtensions
      |
      |@CopyExtensions
      |data class Person(val name: String, val age: Int)
      |
      |val p1 = Person("Alex", 1)
      |val p2 = p1.copyMap(age = { it + 1 })
      |val r = p2.age
      """.compilesWith {
      it.contains("Unused '@CopyExtensions' annotation")
    }
  }

  @Test
  fun `warning for unused annotation, explicit generateAll`() {
    """
      |import at.kopyk.CopyExtensions
      |
      |@CopyExtensions
      |data class Person(val name: String, val age: Int)
      |
      |val p1 = Person("Alex", 1)
      |val p2 = p1.copyMap(age = { it + 1 })
      |val r = p2.age
      """.compilesWith(mapOf("generate" to KopyKatGenerate.ALL)) {
      it.contains("Unused '@CopyExtensions' annotation")
    }
  }

  @Test
  fun `warning for application to non-data class`() {
    """
      |import at.kopyk.CopyExtensions
      |
      |@CopyExtensions
      |class Person(val name: String, val age: Int)
      """.failsWith {
      it.contains("'@CopyExtensions' may only be used")
    }
  }

  @Test
  fun `generates in the given package`() {
    """
      |package A
      |
      |data class Person(val name: String, val age: Int)
      |
      |val p1 = Person("Alex", 1)
      |val p2 = p1.copyMap(age = { it + 1 })
      |val r = p2.age
      """.failsWith(
      mapOf("generate" to "${KopyKatGenerate.PACKAGES_PREFIX}A"),
    ) {
      it.contains("Unresolved reference: copyMap")
    }
  }

  @Test
  fun `generates in the given package, pattern`() {
    """
      |package A
      |
      |data class Person(val name: String, val age: Int)
      |
      |val p1 = Person("Alex", 1)
      |val p2 = p1.copyMap(age = { it + 1 })
      |val r = p2.age
      """.failsWith(
      mapOf("generate" to "${KopyKatGenerate.PACKAGES_PREFIX}?"),
    ) {
      it.contains("Unresolved reference: copyMap")
    }
  }

  @Test
  fun `doesn't generate in other package`() {
    """
      |package B
      |
      |data class Person(val name: String, val age: Int)
      |
      |val p1 = Person("Alex", 1)
      |val p2 = p1.copyMap(age = { it + 1 })
      |val r = p2.age
      """.failsWith(
      mapOf("generate" to "${KopyKatGenerate.PACKAGES_PREFIX}A"),
    ) {
      it.contains("Unresolved reference: copyMap")
    }
  }
}
