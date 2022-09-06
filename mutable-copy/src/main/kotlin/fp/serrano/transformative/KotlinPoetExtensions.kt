package fp.serrano.transformative

import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toKModifier
import com.squareup.kotlinpoet.ksp.toTypeName

fun KSPropertyDeclaration.asParameterSpec(typeParamResolver: TypeParameterResolver): ParameterSpec =
  ParameterSpec(
    name = simpleName.asString(),
    type = type.toTypeName(typeParamResolver),
    modifiers = modifiers.mapNotNull { it.toKModifier() },
  )

fun KSPropertyDeclaration.asPropertySpec(
  typeParamResolver: TypeParameterResolver,
  block: PropertySpec.Builder.() -> Unit = {},
): PropertySpec =
  PropertySpec.builder(
    name = simpleName.asString(),
    type = type.toTypeName(typeParamResolver),
    modifiers = modifiers.mapNotNull { it.toKModifier() },
  ).apply(block).build()

fun TypeSpec.Builder.primaryConstructor(block: FunSpec.Builder.() -> Unit) {
  primaryConstructor(FunSpec.constructorBuilder().apply(block).build())
}

fun FileSpec.Builder.addClass(className: ClassName, block: TypeSpec.Builder.() -> Unit) {
  addType(TypeSpec.classBuilder(className).apply(block).build())
}

fun FileSpec.Builder.addFunction(
  name: String,
  receiver: TypeName? = null,
  returns: TypeName? = null,
  typeVariables: Iterable<TypeVariableName> = emptyList(),
  block: FunSpec.Builder.() -> Unit = {},
) {
  addFunction(FunSpec.builder(name).apply {
    receiver?.apply { receiver(receiver) }
    returns?.apply { returns(returns) }
    addTypeVariables(typeVariables)
  }.apply(block).build())
}

fun buildFile(packageName: String, fileName: String, block: FileSpec.Builder.() -> Unit): FileSpec =
  FileSpec.builder(packageName, fileName).apply(block).build()

fun ClassName.parameterizedWhenNotEmpty(
  typeArguments: List<TypeName>
): TypeName = takeIf { typeArguments.isNotEmpty() }?.parameterizedBy(typeArguments) ?: this
