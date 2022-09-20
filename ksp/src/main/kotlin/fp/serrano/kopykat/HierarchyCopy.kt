@file:Suppress("WildcardImport")
package fp.serrano.kopykat

import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toTypeName
import fp.serrano.kopykat.utils.*

internal val KSClassDeclaration.HierarchyCopyFunctionKt: FileSpec
  get() = onClassScope {
    buildFile(packageName = packageName, kopyKatMapFileName) {
      addGeneratedMarker()
      val properties = getAllProperties()
      addFunction(
        name = "copy",
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
              type = typeName,
            ).defaultValue("this.${property.name}").build()
          )
          "${property.name} = ${property.name}"
        }
        addReturn(
          repeatOnSubclasses("copy(${propertyStatements.joinToString()})")
        )
      }
    }
  }

public fun KSClassDeclaration.repeatOnSubclasses(line: String): String = when {
  isDataClass() -> line
  else -> getSealedSubclasses().map { klass ->
    "is ${klass.name} -> $line"
  }.joinToString(prefix = "when (this) {\n", separator = "\n  ", postfix = "\n}")
}
