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
          .filter { it.typeCategory is Known }
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
}
