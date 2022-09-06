package fp.serrano.transformative

import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toKModifier
import com.squareup.kotlinpoet.ksp.toTypeName

fun KSPropertyDeclaration.toParameterSpec(typeParamResolver: TypeParameterResolver): ParameterSpec =
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
  primaryConstructor(buildConstructor(block))
}

fun FileSpec.Builder.addClass(className: ClassName, block: TypeSpec.Builder.() -> Unit) {
  addType(buildClass(className, block))
}

fun FileSpec.Builder.addFunction(name: String, block: FunSpec.Builder.() -> Unit) {
  addFunction(buildFunction(name, block))
}

fun buildFile(packageName: String, fileName: String, block: FileSpec.Builder.() -> Unit): FileSpec =
  FileSpec.builder(packageName, fileName).apply(block).build()

fun buildClass(className: ClassName, block: TypeSpec.Builder.() -> Unit): TypeSpec =
  TypeSpec.classBuilder(className).apply(block).build()

fun buildConstructor(block: FunSpec.Builder.() -> Unit): FunSpec =
  FunSpec.constructorBuilder().apply(block).build()

fun buildFunction(name: String, block: FunSpec.Builder.() -> Unit): FunSpec =
  FunSpec.builder(name).apply(block).build()

fun ClassName.parameterizedWhenNotEmpty(
  typeArguments: List<TypeName>
): TypeName = takeIf { typeArguments.isNotEmpty() }?.parameterizedBy(typeArguments) ?: this
