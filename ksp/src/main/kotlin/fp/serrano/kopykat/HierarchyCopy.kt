@file:Suppress("WildcardImport")

package fp.serrano.kopykat

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ksp.toTypeName
import fp.serrano.kopykat.utils.FileCompilerScope
import fp.serrano.kopykat.utils.TypeCompileScope
import fp.serrano.kopykat.utils.addDslMarkerClass
import fp.serrano.kopykat.utils.addGeneratedMarker
import fp.serrano.kopykat.utils.kotlin.poet.buildFile
import fp.serrano.kopykat.utils.mapSealedSubclasses
import fp.serrano.kopykat.utils.name

internal fun TypeCompileScope.hierarchyCopyFunctionKt(): FileSpec =
  buildFile(kopyKatCopyFileName) {
    file.addGeneratedMarker()
    addDslMarkerClass()
    addMutableCopy()
    addFreezeFunction {
      val assignments = properties.map { it.toAssignment(".freeze()") }.joinToString()
      addReturn(mapSealedSubclasses { "is ${it.name} -> old.copy(${assignments})" }.joinWithWhen(subject = "old"))
    }
    addToMutateFunction()
    addCopyClosure()

    addRetrofittedCopyFunction()
  }

private val TypeCompileScope.kopyKatCopyFileName get() = "${targetTypeName}CopyKopyKat"

private fun FileCompilerScope.addRetrofittedCopyFunction() {
  file.addFunction(
    name = "copy",
    receiver = targetClassName,
    returns = targetClassName,
    typeVariables = typeVariableNames,
  ) {
    val propertyStatements = properties.map { property ->
      val typeName = property.type.toTypeName(typeParamResolver)
      addParameter(
        ParameterSpec.builder(name = property.name, type = typeName).defaultValue("this.${property.name}").build()
      )
      "${property.name} = ${property.name}"
    }.toList()

    addReturn(mapSealedSubclasses {
      "is ${it.name} -> this.copy(${propertyStatements.joinToString()})"
    }.joinWithWhen())
  }
}