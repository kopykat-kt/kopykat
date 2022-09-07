package fp.serrano.transformative

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeVariableName
import com.squareup.kotlinpoet.ksp.writeTo
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy


class Transformative(private val codegen: CodeGenerator, private val logger: KSPLogger) : SymbolProcessor {
  companion object {
    const val ANNOTATION_NAME = "fp.serrano.transformative"
  }

  override fun process(resolver: Resolver): List<KSAnnotated> {
    resolver
      .getSymbolsWithAnnotation(ANNOTATION_NAME)
      .filterIsInstance<KSClassDeclaration>()
      .forEach(::processClass)

    return emptyList()
  }

  private fun processClass(klass: KSClassDeclaration) {
    if (Modifier.DATA !in klass.modifiers || klass.primaryConstructor == null) {
      logger.error(klass.notDataClassErrorMessage, klass)
      return
    }

    val packageName = klass.packageName.asString()
    val targetTypeName = klass.simpleName.asString()
    val typeParameters = klass.typeParameters
    val typeParamResolver = typeParameters.toTypeParameterResolver()
    val typeVariables = typeParameters.map { it.toTypeVariableName() }
    val targetClassName = ClassName(packageName, targetTypeName).parameterizedWhenNotEmpty(typeVariables)
    buildFile(packageName = packageName, "${klass.simpleName.asString()}Transformative") {
      val properties = klass.getAllProperties()
      addFunction(
        name = "transform",
        receiver = targetClassName,
        returns = targetClassName,
        typeVariables = typeVariables,
      ) {
        val propertyStatements = properties.map { property ->
          val typeName = property.type.toTypeName(typeParamResolver)
          addParameter(
            ParameterSpec.builder(
              name = property.name,
              type = LambdaTypeName.get(parameters = arrayOf(typeName), returnType = typeName)
            ).defaultValue("{ it }").build()
          )
          when {
            typeName.extendsFrom<List<*>>() -> {
              addListParameter(typeName, property)
              "${property.name} = ${property.name}(this.${property.name}).map(${property.name}Each)"
            }

            typeName.extendsFrom<Map<*, *>>() -> {
              addMapParameter(typeName, property)
              "${property.name} = ${property.name}(this.${property.name}).mapValues(${property.name}Each)"
            }

            else -> "${property.name} = ${property.name}(this.${property.name})"
          }
        }
        addCode("return $targetClassName(${propertyStatements.joinToString()})")
      }
    }.writeTo(codeGenerator = codegen, aggregating = false)
  }
}

private fun FunSpec.Builder.addMapParameter(
  typeName: TypeName,
  property: KSPropertyDeclaration
) {
  val (keyType, valueType) = typeName.typeArguments!!
  addParameter(
    ParameterSpec.builder(
      name = property.name + "Each",
      type = LambdaTypeName.get(
        parameters = arrayOf(
          Map.Entry::class.asTypeName().parameterizedBy(keyType, valueType)
        ), returnType = valueType
      )
    ).defaultValue("{ it.value }").build()
  )
}

private fun FunSpec.Builder.addListParameter(
  typeName: TypeName,
  property: KSPropertyDeclaration
) {
  val listType = typeName.typeArguments!!.first()
  addParameter(
    ParameterSpec.builder(
      name = property.name + "Each",
      type = LambdaTypeName.get(parameters = arrayOf(listType), returnType = listType)
    ).defaultValue("{ it }").build()
  )
}

inline fun <reified T> TypeName.extendsFrom(): Boolean =
  this is ParameterizedTypeName && rawType == T::class.asTypeName()

val TypeName.typeArguments get() = (this as? ParameterizedTypeName)?.typeArguments

private val KSDeclaration.name get() = simpleName.asString()
