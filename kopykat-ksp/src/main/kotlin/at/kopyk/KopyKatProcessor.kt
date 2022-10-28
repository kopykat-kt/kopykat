package at.kopyk

import at.kopyk.utils.TypeCategory.Known.Data
import at.kopyk.utils.TypeCategory.Known.Sealed
import at.kopyk.utils.TypeCategory.Known.Value
import at.kopyk.utils.TypeCompileScope
import at.kopyk.utils.hasAnnotation
import at.kopyk.utils.lang.filterIsInstance
import at.kopyk.utils.lang.flatMapRun
import at.kopyk.utils.lang.forEachRun
import at.kopyk.utils.lang.mapRun
import at.kopyk.utils.lang.onEachRun
import at.kopyk.utils.typeCategory
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration

internal interface LoggerScope {
  val logger: KSPLogger
}

internal interface OptionsScope {
  val options: KopyKatOptions
}

internal class ProcessorScope(
  environment: SymbolProcessorEnvironment,
) : LoggerScope, OptionsScope {
  val codegen = environment.codeGenerator
  override val logger = environment.logger
  override val options = KopyKatOptions(environment.logger, environment.options)
}

internal class KopyKatProcessor(
  private val scope: ProcessorScope,
) : SymbolProcessor {

  override fun process(resolver: Resolver): List<KSAnnotated> {
    scope.processFiles(resolver) {
      // add copy functions for data, value classes, and type aliases
      if (options.copyMap || options.mutableCopy) {
        (classes.mapRun { classScope } + typeAliases.mapRun { typealiasScope })
          .filter { it.canHaveCopyFunctions(options.hierarchyCopy) }
          .onEachRun { logger.logging("Processing ${simpleName.asString()}") }
          .forEachRun {
            if (options.copyMap) copyMapFunctionKt.write()
            if (options.mutableCopy) mutableCopyKt.write()
          }
      }
      // add isomorphic copies
      declarations
        .filterIsInstance<KSClassDeclaration> { hasCopyAnnotation() }
        .flatMapRun { classScope.allCopies }
        .distinctBy { it.fileName }
        .mapNotNull(::fileSpec)
        .forEachRun { write() }

    }
    return emptyList()
  }

  private fun TypeCompileScope.canHaveCopyFunctions(hierarchyCopy: Boolean) =
    typeCategory in listOf(Data, Value) || typeCategory is Sealed && hierarchyCopy
}

private fun KSClassDeclaration.hasCopyAnnotation() =
  hasAnnotation<Copy>() || hasAnnotation<CopyTo>() || hasAnnotation<CopyFrom>()
