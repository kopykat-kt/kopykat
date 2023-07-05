package at.kopyk

import org.junit.jupiter.api.Test

/**
 * @author: xiaozhikang
 * @create: 2023/7/3
 */
class MutableCollectionCopyTest {
  @Test
  fun `copy property in collection`() {
    """
      data class Group(val p: List<Person>)
      data class Person(val age: Int)
      
      val g1 = Group(listOf(Person(1), Person(2)))
      val g2 = g1.copy { p[1].age ++ }
      val age = g2.p[1].age
    """.trimIndent().evals("age" to 3)
  }

  @Test
  fun `copy property in collection with generic type`() {
    """
      data class Group(val p: List<Person<String>>)
      data class Person<T>(val mark: T)
      
      val g1 = Group(listOf(Person("old"), Person("old")))
      val g2 = g1.copy { p[1].mark = "new" }
      val mark = g2.p[1].mark
    """.trimIndent().evals("mark" to "new")
  }
}
