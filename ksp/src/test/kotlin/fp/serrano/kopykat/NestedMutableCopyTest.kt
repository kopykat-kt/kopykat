package fp.serrano.kopykat

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
      |value class JobLevel(val level: Int)
      |
      |val p1 = Person("Alex", Job("Developer", Level(1)))
      |val p2 = p1.copy { job.level++ }
      |val r = p2.level.level
      """.evals("r" to 2)
  }
}
