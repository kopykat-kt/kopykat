package at.kopyk

import org.junit.jupiter.api.Test

class NestedMutableCopyTest {

  @Test
  fun `mutate nested property`() {
    """
      |data class Person(val name: String, val job: Job)
      |data class Job(val title: String)
      |
      |val p1 = Person("Alex", Job("Developer"))
      |val p2 = p1.copy { job.title = "Señor Developer" }
      |val r = p2.job.title
      """.evals("r" to "Señor Developer")
  }


  @Test
  fun `mutate nested property with value type`() {
    """
      |data class Person(val name: String, val job: Job)
      |data class Job(val title: String, val level: Level)
      |@JvmInline value class Level(val value: Int)
      |
      |val p1 = Person("Alex", Job("Developer", Level(1)))
      |val p2: Person = p1.copy { job.level.value++ }
      |val r = p2.job.level.value
      """.evals("r" to 2)
  }
}
