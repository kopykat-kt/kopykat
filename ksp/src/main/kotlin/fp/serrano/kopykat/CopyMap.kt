@file:Suppress("WildcardImport")

package fp.serrano.kopykat

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ksp.toTypeName
import fp.serrano.kopykat.utils.TypeCompileScope
import fp.serrano.kopykat.utils.addGeneratedMarker
import fp.serrano.kopykat.utils.getPrimaryConstructorProperties
import fp.serrano.kopykat.utils.name

internal val TypeCompileScope.copyMapFunctionKt: FileSpec
  get() = buildFile(packageName = packageName.asString(), kopyKatFileName) {
    addGeneratedMarker()
    val properties = getPrimaryConstructorProperties()
    addFunction(
      name = "copyMap",
      receiver = targetClassName,
      returns = targetClassName,
      typeVariables = typeVariableNames,
    ) {
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
      addReturn(repeatOnSubclasses(propertyStatements.joinToString(), "copy"))
    }
  }

private val TypeCompileScope.kopyKatFileName get() = "${targetTypeName}KopyKat"
