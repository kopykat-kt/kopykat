package at.kopyk.utils

import at.kopyk.poet.flattenWithSuffix
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName

internal val ClassName.mutable: ClassName get() = flattenWithSuffix("Mutable")
internal val ParameterizedTypeName.mutable: ParameterizedTypeName get() = flattenWithSuffix("Mutable")
internal val ClassName.dslMarker: ClassName get() = flattenWithSuffix("DslMarker")

internal val TypeName.mutable: TypeName?
  get() = when (this) {
    is ClassName -> this.mutable
    is ParameterizedTypeName -> this.mutable
    else -> null
  }
