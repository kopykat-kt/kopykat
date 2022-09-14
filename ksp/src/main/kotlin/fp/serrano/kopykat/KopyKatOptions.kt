package fp.serrano.kopykat

internal data class KopyKatOptions(
  val copyMap: Boolean,
  val mutableCopy: Boolean,
  val valueCopy: Boolean
) {
  companion object {
    fun fromKspOptions(options: Map<String, String>) =
      KopyKatOptions(
        copyMap = options.parseBoolOrTrue("copyMap"),
        mutableCopy = options.parseBoolOrTrue("mutableCopy"),
        valueCopy = options.parseBoolOrTrue("valueCopy")
      )
  }
}

private fun Map<String, String>.parseBoolOrTrue(key: String) =
  this[key]?.lowercase()?.toBooleanStrictOrNull() ?: true
