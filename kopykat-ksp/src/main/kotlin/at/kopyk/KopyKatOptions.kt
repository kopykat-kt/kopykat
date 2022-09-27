package at.kopyk

import com.google.devtools.ksp.processing.KSPLogger

internal sealed interface KopyKatGenerate {
  object Error : KopyKatGenerate
  object Annotated : KopyKatGenerate
  sealed interface NotAnnotated : KopyKatGenerate
  object All : NotAnnotated
  data class Packages(val patterns: List<String>) : NotAnnotated

  companion object {
    const val ALL = "all"
    const val ANNOTATED = "annotated"
    const val PACKAGES_PREFIX = "packages"

    fun fromKspOptions(logger: KSPLogger, generate: String?): KopyKatGenerate = when {
      generate == null -> All
      generate == ALL -> All
      generate == ANNOTATED -> Annotated
      generate.startsWith(PACKAGES_PREFIX) ->
        Packages(generate.split(':').drop(1))
      else -> {
        logger.error("Unrecognized value for 'generate'", null)
        Error // return something, although the error is reported
      }
    }
  }
}

internal data class KopyKatOptions(
  val copyMap: Boolean,
  val mutableCopy: Boolean,
  val hierarchyCopy: Boolean,
  val generate: KopyKatGenerate
) {
  companion object {
    const val COPY_MAP = "copyMap"
    const val MUTABLE_COPY = "mutableCopy"
    const val HIERARCHY_COPY = "hierarchyCopy"
    const val GENERATE = "generate"

    fun fromKspOptions(logger: KSPLogger, options: Map<String, String>) =
      KopyKatOptions(
        copyMap = options.parseBoolOrTrue(COPY_MAP),
        mutableCopy = options.parseBoolOrTrue(MUTABLE_COPY),
        hierarchyCopy = options.parseBoolOrTrue(HIERARCHY_COPY),
        generate = KopyKatGenerate.fromKspOptions(logger, options[GENERATE])
      )
  }
}

private fun Map<String, String>.parseBoolOrTrue(key: String) =
  this[key]?.lowercase()?.toBooleanStrictOrNull() ?: true
