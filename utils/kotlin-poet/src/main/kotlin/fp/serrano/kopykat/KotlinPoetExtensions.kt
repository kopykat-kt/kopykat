package fp.serrano.kopykat

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.ksp.toKModifier
import com.squareup.kotlinpoet.ksp.writeTo

private val notWantedModifiers: List<Modifier> =
  listOf(Modifier.OVERRIDE, Modifier.OPEN, Modifier.ABSTRACT)

public fun KSPropertyDeclaration.asParameterSpec(typeName: TypeName): ParameterSpec =
  ParameterSpec(
    name = simpleName.asString(),
    type = typeName,
    modifiers = modifiers.mapNotNull { it.takeIf { it !in notWantedModifiers }?.toKModifier() },
  )

public fun KSPropertyDeclaration.asPropertySpec(
  typeName: TypeName,
  block: PropertySpec.Builder.() -> Unit = {},
): PropertySpec =
  PropertySpec.builder(
    name = simpleName.asString(),
    type = typeName,
    modifiers = modifiers.mapNotNull { it.takeIf { it !in notWantedModifiers }?.toKModifier() },
  ).apply(block).build()

public fun TypeSpec.Builder.primaryConstructor(block: FunSpec.Builder.() -> Unit) {
  primaryConstructor(FunSpec.constructorBuilder().apply(block).build())
}

public fun FileSpec.Builder.addClass(className: ClassName, block: TypeSpec.Builder.() -> Unit) {
  addType(TypeSpec.classBuilder(className).apply(block).build())
}

public fun buildFile(packageName: String, fileName: String, block: FileSpec.Builder.() -> Unit): FileSpec =
  FileSpec.builder(packageName, fileName).apply(block).build()

public fun ClassName.parameterizedWhenNotEmpty(
  typeArguments: List<TypeName>
): TypeName = takeIf { typeArguments.isNotEmpty() }?.parameterizedBy(typeArguments) ?: this

public fun FileSpec.writeTo(codeGenerator: CodeGenerator) {
  writeTo(codeGenerator = codeGenerator, aggregating = false)
}

public fun FunSpec.Builder.addReturn(expr: String, vararg args: Any?): FunSpec.Builder =
  addCode("return $expr", args)

public fun ClassName.map(name: (String) -> String): ClassName =
  ClassName(packageName, simpleNames.run { dropLast(1) + name(last()) })

public val KSDeclaration.className: ClassName
  get() = ClassName(packageName = packageName.asString(), simpleName.asString())

public fun TypeName.asTransformLambda(): LambdaTypeName =
  LambdaTypeName.get(parameters = arrayOf(this), returnType = this)

public fun TypeName.asReceiverConsumer(): LambdaTypeName =
  LambdaTypeName.get(receiver = this, returnType = UNIT)
