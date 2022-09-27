package at.kopyk

internal data class KopyKatOptions(
  val copyMap: Boolean,
  val mutableCopy: Boolean,
  val hierarchyCopy: Boolean,
  val generateAll: Boolean
) {
  companion object {
    const val COPY_MAP = "copyMap"
    const val MUTABLE_COPY = "mutableCopy"
    const val HIERARCHY_COPY = "hierarchyCopy"
    const val GENERATE_ALL = "generateAll"

    fun fromKspOptions(options: Map<String, String>) =
      KopyKatOptions(
        copyMap = options.parseBoolOrTrue(COPY_MAP),
        mutableCopy = options.parseBoolOrTrue(MUTABLE_COPY),
        hierarchyCopy = options.parseBoolOrTrue(HIERARCHY_COPY),
        generateAll = options.parseBoolOrTrue(GENERATE_ALL)
      )
  }
}

private fun Map<String, String>.parseBoolOrTrue(key: String) =
  this[key]?.lowercase()?.toBooleanStrictOrNull() ?: true
