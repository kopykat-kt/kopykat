@file:Suppress("WildcardImport")
package fp.serrano.kopykat

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import fp.serrano.kopykat.utils.*

internal class KopyKatProcessor(
  private val codegen: CodeGenerator,
  private val logger: KSPLogger,
  private val options: KopyKatOptions
) : SymbolProcessor {

  override fun process(resolver: Resolver): List<KSAnnotated> {
    with(resolver.getAllFiles()) {
      if (none { file -> file.hasGeneratedMarker() }) {
        flatMap { file -> file.declarations }
          .filterIsInstance<KSClassDeclaration>()
          .forEach {
            logger.logging("Processing ${it.simpleName}", it)
            it.process()
          }
      }
    }
    return emptyList()
  }


  private fun KSClassDeclaration.process() {
    when {
      isDataClass() -> {
        if (options.copyMap) CopyMapFunctionKt.writeTo(codegen)
        if (options.mutableCopy) MutableCopyKt.writeTo(codegen)
      }
      isValueClass() -> {
        if (options.valueCopy) ValueCopyFunctionKt.writeTo(codegen)
      }
    }
  }
}

private fun KSClassDeclaration.isDataClass() =
  Modifier.DATA in modifiers && primaryConstructor != null

private fun KSClassDeclaration.isValueClass() =
  Modifier.VALUE in modifiers && primaryConstructor != null
