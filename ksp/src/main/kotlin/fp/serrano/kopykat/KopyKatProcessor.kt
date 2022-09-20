@file:Suppress("WildcardImport")
package fp.serrano.kopykat

import com.google.devtools.ksp.isPublic
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
        val targets = flatMap { file -> file.declarations }
          .filterIsInstance<KSClassDeclaration>()
          .filter { it.isConstructable() && (it.isDataClass() || it.isValueClass()) }
        val mutableCandidates = targets//.map { it.asStarProjectedType() }
        targets
          .onEach { logger.logging("Processing ${it.simpleName}", it) }
          .forEach { it.process(mutableCandidates) }
      }
    }
    return emptyList()
  }

  private fun KSClassDeclaration.process(mutableCandidates: Sequence<KSClassDeclaration>) {
    when {
      isDataClass() -> {
        if (options.copyMap) copyMapFunctionKt().writeTo(codegen)
        if (options.mutableCopy) mutableCopyKt(mutableCandidates).writeTo(codegen)
      }
      isValueClass() -> {
        if (options.valueCopy) ValueCopyFunctionKt.writeTo(codegen)
      }
    }
  }
}

private fun KSClassDeclaration.isConstructable() = primaryConstructor?.isPublic() == true
private fun KSModifierListOwner.isDataClass() = Modifier.DATA in modifiers
internal fun KSModifierListOwner.isValueClass() = Modifier.VALUE in modifiers
