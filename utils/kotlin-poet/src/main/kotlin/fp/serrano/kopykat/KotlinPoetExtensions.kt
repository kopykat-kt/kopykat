package fp.serrano.kopykat

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toKModifier
import com.squareup.kotlinpoet.ksp.writeTo

public fun KSPropertyDeclaration.asParameterSpec(typeName: TypeName): ParameterSpec =
  ParameterSpec(
    name = simpleName.asString(),
    type = typeName,
    modifiers = modifiers.mapNotNull { it.toKModifier() },
  )

public fun KSPropertyDeclaration.asPropertySpec(
  typeName: TypeName,
  block: PropertySpec.Builder.() -> Unit = {},
): PropertySpec =
  PropertySpec.builder(
    name = simpleName.asString(),
    type = typeName,
    modifiers = modifiers.mapNotNull { it.toKModifier() },
  ).apply(block).build()

public fun TypeSpec.Builder.primaryConstructor(block: FunSpec.Builder.() -> Unit) {
  primaryConstructor(FunSpec.constructorBuilder().apply(block).build())
}

public fun FileSpec.Builder.addClass(className: ClassName, block: TypeSpec.Builder.() -> Unit) {
  addType(TypeSpec.classBuilder(className).apply(block).build())
}

public fun FileSpec.Builder.addFunction(
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

public fun buildFile(packageName: String, fileName: String, block: FileSpec.Builder.() -> Unit): FileSpec =
  FileSpec.builder(packageName, fileName).apply(block).build()

public fun ClassName.parameterizedWhenNotEmpty(
  typeArguments: List<TypeName>
): TypeName = takeIf { typeArguments.isNotEmpty() }?.parameterizedBy(typeArguments) ?: this

public fun FileSpec.writeTo(codeGenerator: CodeGenerator) {
  writeTo(codeGenerator = codeGenerator, aggregating = false)
}
