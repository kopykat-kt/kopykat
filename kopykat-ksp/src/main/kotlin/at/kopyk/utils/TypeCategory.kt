package at.kopyk.utils

import at.kopyk.utils.TypeCategory.Known
import at.kopyk.utils.TypeCategory.Unknown
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.symbol.Modifier.SEALED

internal val KSClassDeclaration.typeCategory: TypeCategory
  get() = when {
    isDataClass() -> Known.Data
    isValueClass() -> Known.Value
    isSealedDataHierarchy() -> Known.Sealed
    else -> Unknown(this)
  }

internal inline fun TypeCompileScope.onKnownCategory(block: (Known) -> Unit) {
  (typeCategory as? Known)?.apply(block) ?: logger.error("Type $fullName is not supported by KopyKat")
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
  SEALED in modifiers && isAbstract() && hasOnlyDataClassChildren()

private fun KSClassDeclaration.hasOnlyDataClassChildren() =
  sealedTypes.any() && sealedTypes.all { it.isDataClass() || it.isValueClass() }
