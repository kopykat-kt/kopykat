package at.kopyk.poet

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.ksp.toKModifier
import com.squareup.kotlinpoet.ksp.writeTo

private val notWantedModifiers: List<Modifier> =
  listOf(Modifier.OVERRIDE, Modifier.OPEN, Modifier.ABSTRACT)

private val modifiersAllowedInParams: List<Modifier> =
  listOf(Modifier.VARARG, Modifier.NOINLINE, Modifier.CROSSINLINE)

public val KSPropertyDeclaration.propertyModifiers: List<KModifier>
  get() = modifiers.mapNotNull { it.takeIf { it !in notWantedModifiers }?.toKModifier() }

public val KSPropertyDeclaration.parameterModifiers: List<KModifier>
  get() = modifiers.mapNotNull { it.takeIf { it in modifiersAllowedInParams }?.toKModifier() }

public fun ClassName.parameterizedWhenNotEmpty(typeArguments: List<TypeName>): TypeName =
  takeIf { typeArguments.isNotEmpty() }?.parameterizedBy(typeArguments) ?: this

public fun FileSpec.writeTo(codeGenerator: CodeGenerator) {
  writeTo(codeGenerator = codeGenerator, aggregating = false)
}

public fun ClassName.map(name: (String) -> String): ClassName =
  ClassName(packageName, simpleNames.run { dropLast(1) + name(last()) })

public fun ClassName.append(name: String): ClassName =
  ClassName(packageName, simpleNames + name)

public val KSDeclaration.className: ClassName
  get() =
    when (val parent = parentDeclaration) {
      is KSClassDeclaration -> parent.className.append(simpleName.asString())
      else -> ClassName(packageName = packageName.asString(), simpleName.asString())
    }

public fun TypeName.asTransformLambda(): LambdaTypeName =
  LambdaTypeName.get(parameters = arrayOf(this), returnType = this)

public fun TypeName.asReceiverConsumer(): LambdaTypeName =
  LambdaTypeName.get(receiver = this, returnType = UNIT)

public fun ClassName.flattenWithSuffix(suffix: String): ClassName {
  val mutableSimpleName = (simpleNames + suffix).joinToString(separator = "$")
  return ClassName(packageName, mutableSimpleName)
}
