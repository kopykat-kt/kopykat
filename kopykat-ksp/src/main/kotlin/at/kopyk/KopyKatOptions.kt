package at.kopyk

import com.google.devtools.ksp.processing.KSPLogger

internal sealed interface KopyKatGenerate {
  data object Error : KopyKatGenerate

  data object Annotated : KopyKatGenerate

  sealed interface NotAnnotated : KopyKatGenerate

  data object All : NotAnnotated

  data class Packages(val patterns: List<String>) : NotAnnotated

  companion object {
    const val ALL = "all"
    const val ANNOTATED = "annotated"
    const val PACKAGES_PREFIX = "packages"

    fun fromKspOptions(
      logger: KSPLogger,
      generate: String?,
    ): KopyKatGenerate =
      when {
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
  val generate: KopyKatGenerate,
)

@Suppress("FunctionName")
internal fun KopyKatOptions(
  logger: KSPLogger,
  options: Map<String, String>,
) = KopyKatOptions(
  copyMap = options.parseBoolOrTrue(COPY_MAP),
  mutableCopy = options.parseBoolOrTrue(MUTABLE_COPY),
  hierarchyCopy = options.parseBoolOrTrue(HIERARCHY_COPY),
  generate = KopyKatGenerate.fromKspOptions(logger, options[GENERATE]),
)

private const val COPY_MAP = "copyMap"
private const val MUTABLE_COPY = "mutableCopy"
private const val HIERARCHY_COPY = "hierarchyCopy"
private const val GENERATE = "generate"

private fun Map<String, String>.parseBoolOrTrue(key: String) = this[key]?.lowercase()?.toBooleanStrictOrNull() ?: true
