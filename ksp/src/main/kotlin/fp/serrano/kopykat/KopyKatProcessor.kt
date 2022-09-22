package fp.serrano.kopykat

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import fp.serrano.kopykat.utils.ClassCompileScope
import fp.serrano.kopykat.utils.TypeCategory.Known
import fp.serrano.kopykat.utils.TypeCategory.Known.Data
import fp.serrano.kopykat.utils.TypeCategory.Known.Sealed
import fp.serrano.kopykat.utils.TypeCategory.Known.Value
import fp.serrano.kopykat.utils.TypeCompileScope
import fp.serrano.kopykat.utils.hasGeneratedMarker
import fp.serrano.kopykat.utils.lang.forEachRun
import fp.serrano.kopykat.utils.onKnownCategory
import fp.serrano.kopykat.utils.typeCategory

internal class KopyKatProcessor(
  private val codegen: CodeGenerator,
  private val logger: KSPLogger,
  private val options: KopyKatOptions
) : SymbolProcessor {

  override fun process(resolver: Resolver): List<KSAnnotated> {
    resolver.getAllFiles().let { files ->
      if (files.none { it.hasGeneratedMarker() }) {
        files.flatMap { it.declarations }
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
