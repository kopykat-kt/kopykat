@file:Suppress("WildcardImport")
package fp.serrano.kopykat

import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toTypeName
import fp.serrano.kopykat.utils.*

internal val KSClassDeclaration.CopyMapFunctionKt: FileSpec
  get() = onClassScope {
    buildFile(packageName = packageName, transformativeFileName) {
      addGeneratedMarker()
      val properties = getAllProperties()
      addFunction(
        name = "copyMap",
        receiver = targetClassName,
        returns = targetClassName,
        typeVariables = typeVariableNames,
      ) {
        addModifiers(KModifier.INLINE)
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
