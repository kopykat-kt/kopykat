package at.kopyk.poet

import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName

public fun TypeVariableName.makeInvariant(): TypeVariableName = TypeVariableName(name, bounds, null)

public fun TypeName.makeInvariant(): TypeName =
  when (this) {
    is TypeVariableName -> TypeVariableName(name, bounds, null)
    is ParameterizedTypeName ->
      copy(
        typeArguments = typeArguments.map { it.makeInvariant() },
      )
    else -> this
  }
