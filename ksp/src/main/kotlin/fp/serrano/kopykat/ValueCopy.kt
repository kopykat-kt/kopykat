@file:Suppress("WildcardImport")
package fp.serrano.kopykat

import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toTypeName
import fp.serrano.kopykat.utils.*

internal val KSClassDeclaration.ValueCopyFunctionKt: FileSpec
  get() = onClassScope {
    buildFile(packageName = packageName, kopyKatFileName) {
      addGeneratedMarker()
      val properties = getAllProperties()
      addFunction(
        name = "copy",
        receiver = targetClassName,
        returns = targetClassName,
        typeVariables = typeVariableNames,
      ) {
        // properties should be a singleton list,
        // but this allows sharing logic with 'transform'
        val propertyStatements = properties.map { property ->
          val typeName = property.type.toTypeName(typeParamResolver)
          addParameter(
            ParameterSpec.builder(
              name = property.name,
              type = LambdaTypeName.get(parameters = arrayOf(typeName), returnType = typeName)
            ).defaultValue("{ it }").build()
          )
          "${property.name} = ${property.name}(this.${property.name})"
        }
        addCode("return $targetClassName(${propertyStatements.joinToString()})")
      }
    }
  }
