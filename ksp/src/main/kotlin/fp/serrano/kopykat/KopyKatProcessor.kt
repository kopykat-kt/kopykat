package fp.serrano.kopykat

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import fp.serrano.kopykat.utils.TypeCompileScope
import fp.serrano.kopykat.utils.hasGeneratedMarker
import fp.serrano.kopykat.utils.isDataClass
import fp.serrano.kopykat.utils.isSealedDataHierarchy
import fp.serrano.kopykat.utils.isValueClass
import fp.serrano.kopykat.utils.ksp.TypeCategory.Known.Data
import fp.serrano.kopykat.utils.ksp.TypeCategory.Known.Sealed
import fp.serrano.kopykat.utils.ksp.TypeCategory.Known.Value
import fp.serrano.kopykat.utils.ksp.onKnownCategory
import fp.serrano.kopykat.utils.onClassScope

internal class KopyKatProcessor(
  private val codegen: CodeGenerator,
  private val logger: KSPLogger,
  private val options: KopyKatOptions
) : SymbolProcessor {

  override fun process(resolver: Resolver): List<KSAnnotated> {
    resolver.getAllFiles().let { files ->
      if (files.none { it.hasGeneratedMarker() }) {
        val targets = files.flatMap { it.declarations }
          .filterIsInstance<KSClassDeclaration>()
          .filter { it.requiresProcessing() }
        targets
          .onEach { logger.logging("Processing ${it.simpleName}", it) }
          .forEach { it.onClassScope(targets, logger) { process() } }
      }
    }
    return emptyList()
  }

  private fun TypeCompileScope.process() {
    fun generate() {
      if (options.copyMap) copyMapFunctionKt.writeTo(codegen)
      if (options.mutableCopy) mutableCopyKt().writeTo(codegen)
    }
    onKnownCategory { category ->
      when (category) {
        Data, Value -> generate()
        Sealed -> if (options.hierarchyCopy) generate()
      }
    }
  }
}

private fun KSClassDeclaration.requiresProcessing() =
  isDataClass() || isValueClass() || isSealedDataHierarchy()
