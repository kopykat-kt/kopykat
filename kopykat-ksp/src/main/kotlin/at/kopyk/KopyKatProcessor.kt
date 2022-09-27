package at.kopyk

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import at.kopyk.poet.writeTo
import at.kopyk.utils.ClassCompileScope
import at.kopyk.utils.TypeCategory.Known
import at.kopyk.utils.TypeCategory.Known.Data
import at.kopyk.utils.TypeCategory.Known.Sealed
import at.kopyk.utils.TypeCategory.Known.Value
import at.kopyk.utils.TypeCompileScope
import at.kopyk.utils.allNestedDeclarations
import at.kopyk.utils.hasGeneratedMarker
import at.kopyk.utils.lang.forEachRun
import at.kopyk.utils.onKnownCategory
import at.kopyk.utils.typeCategory
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent

internal class KopyKatProcessor(
  private val codegen: CodeGenerator,
  private val logger: KSPLogger,
  private val options: KopyKatOptions
) : SymbolProcessor {

  override fun process(resolver: Resolver): List<KSAnnotated> {
    resolver.getAllFiles().let { files ->
      if (files.none { it.hasGeneratedMarker() }) {
        files.flatMap { it.allNestedDeclarations() }
          .filterIsInstance<KSClassDeclaration>()
          .onEach { it.checkAnnotationMisuse() }
          .filter { it.shouldGenerate() && it.typeCategory is Known }
          .let { targets -> targets.map { ClassCompileScope(it, targets, logger) } }
          .forEachRun { process() }
      }
    }
    return emptyList()
  }

  private fun TypeCompileScope.process() {
    logger.logging("Processing $simpleName")
    fun generate() {
      if (options.copyMap) copyMapFunctionKt.writeTo(codegen)
      if (options.mutableCopy) mutableCopyKt.writeTo(codegen)
    }
    onKnownCategory { category ->
      when (category) {
        Data, Value -> generate()
        Sealed -> if (options.hierarchyCopy) generate()
      }
    }
  }

  @OptIn(KspExperimental::class)
  private fun KSClassDeclaration.shouldGenerate(): Boolean =
    options.generateAll || isAnnotationPresent(CopyExtensions::class)

  @OptIn(KspExperimental::class)
  private fun KSClassDeclaration.checkAnnotationMisuse() {
    if (isAnnotationPresent(CopyExtensions::class)) {
      if (typeCategory !is Known) {
        logger.error("""
          '@CopyExtensions' may only be used in data or value classes,
          or sealed hierarchies of those.
        """.trimIndent(), this)
        return
      }
      if (options.generateAll) {
        logger.warn("""
          Unused '@CopyExtensions' annotation, the plug-in is configured to process all classes.
          Add 'arg("annotatedOnly", "true")' to your KSP configuration to change this option.
          More info at https://kopyk.at/#enable-only-for-selected-classes.
        """.trimIndent(), this)
        return
      }
    }
  }
}
