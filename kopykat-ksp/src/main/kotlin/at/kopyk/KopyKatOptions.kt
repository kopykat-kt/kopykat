package at.kopyk

internal data class KopyKatOptions(
  val copyMap: Boolean,
  val mutableCopy: Boolean,
  val hierarchyCopy: Boolean,
  val annotatedOnly: Boolean
) {
  companion object {
    fun fromKspOptions(options: Map<String, String>) =
      KopyKatOptions(
        copyMap = options.parseBoolOrTrue("copyMap"),
        mutableCopy = options.parseBoolOrTrue("mutableCopy"),
        hierarchyCopy = options.parseBoolOrTrue("hierarchyCopy"),
        annotatedOnly = options.parseBoolOrFalse("annotatedOnly")
      )
  }
}

private fun Map<String, String>.parseBoolOrTrue(key: String) =
  this[key]?.lowercase()?.toBooleanStrictOrNull() ?: true

private fun Map<String, String>.parseBoolOrFalse(key: String) =
  this[key]?.lowercase()?.toBooleanStrictOrNull() ?: false
