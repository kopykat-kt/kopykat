package fp.serrano.kopykat.utils.ksp

import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import fp.serrano.kopykat.utils.TypeCompileScope
import fp.serrano.kopykat.utils.ksp.TypeCategory.Known
import fp.serrano.kopykat.utils.ksp.TypeCategory.Unknown
import fp.serrano.kopykat.utils.name
import fp.serrano.kopykat.utils.sealedTypes

internal val KSClassDeclaration.category: TypeCategory
  get() = when {
    isDataClass() -> Known.Data
    isValueClass() -> Known.Value
    isSealedDataHierarchy() -> Known.Sealed
    else -> Unknown(this)
  }

internal inline fun TypeCompileScope.onKnownCategory(block: (Known) -> Unit) {
  (category as? Known)?.apply(block) ?: logger.error("Type $name is not supported by KopyKat")
}

internal sealed interface TypeCategory {
  sealed interface Known : TypeCategory {
    object Sealed : Known
    object Value : Known
    object Data : Known
  }

  @JvmInline value class Unknown(val original: KSClassDeclaration) : TypeCategory
}

private fun KSClassDeclaration.isConstructable() = primaryConstructor?.isPublic() == true

private fun KSClassDeclaration.isDataClass() = isConstructable() && Modifier.DATA in modifiers

private fun KSClassDeclaration.isValueClass() = isConstructable() && Modifier.VALUE in modifiers

private fun KSClassDeclaration.isSealedDataHierarchy() =
  Modifier.SEALED in modifiers && isAbstract() && hasOnlyDataClassChildren()

private fun KSClassDeclaration.hasOnlyDataClassChildren() =
  sealedTypes.all { it.isDataClass() || it.isValueClass() }