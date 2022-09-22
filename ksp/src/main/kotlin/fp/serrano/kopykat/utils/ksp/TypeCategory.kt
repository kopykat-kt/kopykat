package fp.serrano.kopykat.utils.ksp

import com.google.devtools.ksp.symbol.KSClassDeclaration
import fp.serrano.kopykat.utils.TypeCompileScope
import fp.serrano.kopykat.utils.isDataClass
import fp.serrano.kopykat.utils.isSealedDataHierarchy
import fp.serrano.kopykat.utils.isValueClass
import fp.serrano.kopykat.utils.ksp.TypeCategory.Known
import fp.serrano.kopykat.utils.ksp.TypeCategory.Unknown
import fp.serrano.kopykat.utils.name

internal val TypeCompileScope.category: TypeCategory
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