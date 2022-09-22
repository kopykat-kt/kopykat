@file:Suppress("WildcardImport")

package fp.serrano.kopykat

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ksp.toTypeName
import fp.serrano.kopykat.utils.TypeCompileScope
import fp.serrano.kopykat.utils.addGeneratedMarker
import fp.serrano.kopykat.utils.isDataClass
import fp.serrano.kopykat.utils.isValueClass
import fp.serrano.kopykat.utils.mapSealedSubclasses
import fp.serrano.kopykat.utils.name

internal val TypeCompileScope.copyMapFunctionKt: FileSpec
  get() = buildFile(packageName = packageName.asString(), kopyKatFileName) {
    addGeneratedMarker()
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

private fun KSClassDeclaration.repeatOnSubclasses(
  line: String,
  functionName: String,
): String = when {
  isValueClass() -> "${name}($line)"
  isDataClass() -> "$functionName(${line})"
  else -> mapSealedSubclasses { "is ${it.name} -> $functionName($line)" }.joinWithWhen()
}

internal fun Sequence<String>.joinWithWhen(subject: String = "this") =
  joinToString(prefix = "when ($subject) {\n", separator = "\n", postfix = "\n}")
