package fp.serrano.kopykat.utils

import com.google.devtools.ksp.symbol.KSDeclaration
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName

internal inline fun <reified T> TypeName.extendsFrom(): Boolean =
  this is ParameterizedTypeName && rawType == T::class.asTypeName()

internal val TypeName.typeArguments get() = (this as? ParameterizedTypeName)?.typeArguments

internal val KSDeclaration.name get() = simpleName.asString()
