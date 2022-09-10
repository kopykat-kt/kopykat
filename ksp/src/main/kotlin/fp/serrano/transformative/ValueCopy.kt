package fp.serrano.transformative

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ksp.toTypeName
import fp.serrano.transformative.utils.addGeneratedMarker
import fp.serrano.transformative.utils.name
import fp.serrano.transformative.utils.onClassScope

internal val KSClassDeclaration.ValueCopyFunctionKt: FileSpec
  get() = onClassScope {
    buildFile(packageName = packageName, transformativeFileName) {
      addGeneratedMarker()
      val properties = getAllProperties()
      addFunction(
        name = "copy",
        receiver = targetClassName,
        returns = targetClassName,
        typeVariables = typeVariableNames,
      ) {
        addModifiers(KModifier.INLINE)
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
