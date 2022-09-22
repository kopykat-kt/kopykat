@file:Suppress("WildcardImport")

package fp.serrano.kopykat

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import fp.serrano.kopykat.utils.FileCompilerScope
import fp.serrano.kopykat.utils.TypeCompileScope
import fp.serrano.kopykat.utils.addDslMarkerClass
import fp.serrano.kopykat.utils.addGeneratedMarker
import fp.serrano.kopykat.utils.annotationClassName
import fp.serrano.kopykat.utils.asReceiverConsumer
import fp.serrano.kopykat.utils.kotlin.poet.buildFile
import fp.serrano.kopykat.utils.ksp.TypeCategory.Known.Data
import fp.serrano.kopykat.utils.ksp.TypeCategory.Known.Sealed
import fp.serrano.kopykat.utils.ksp.TypeCategory.Known.Value
import fp.serrano.kopykat.utils.ksp.category
import fp.serrano.kopykat.utils.ksp.onKnownCategory
import fp.serrano.kopykat.utils.name
import fp.serrano.kopykat.utils.sealedTypes

internal val TypeCompileScope.mutableCopyKt: FileSpec
  get() = buildFile(mutable.simpleName) {
    file.addGeneratedMarker()

    addDslMarkerClass()
    addMutableCopy()
    addFreezeFunction()
    addToMutateFunction()
    addCopyClosure()

    if (category is Sealed) {
      addRetrofittedCopyFunction()
    }
  }

internal fun FileCompilerScope.addMutableCopy() {
  file.addClass(mutable) {
    addAnnotation(annotationClassName)
    addTypeVariables(typeVariableNames)
    primaryConstructor {
      properties.forEach { property ->
        val typeName = property.type.resolve().takeIf { it.hasMutableCopy() }
          ?.run { ClassName(declaration.packageName.asString(), "Mutable${declaration.name}") }
          ?: property.typeName

        addParameter(property.asParameterSpec(typeName))
        addProperty(property.asPropertySpec(typeName) { mutable(true).initializer(property.name) })
      }
      addParameter(name = "old", type = target.parameterized)
      addProperty(PropertySpec.builder(name = "old", type = target.parameterized).initializer("old").build())
    }
  }
}

internal fun FileCompilerScope.addFreezeFunction() {
  onKnownCategory { category ->
    addFunction(name = "freeze", receives = mutable.parameterized, returns = target.parameterized) {
      addReturn(
        when (category) {
          Data, Value -> "${target.simpleName}(${properties.joinAsAssignments(".freeze()")})"
          Sealed -> sealedTypes.joinWithWhen(subject = "old") {
            "is ${it.name} -> old.copy(${properties.joinAsAssignments(".freeze()")})"
          }
        }
      )
    }
  }
}

internal fun FileCompilerScope.addToMutateFunction() {
  addFunction(name = "toMutable", receives = target.parameterized, returns = mutable.parameterized) {
    addReturn("${mutable.parameterized}(old = this, ${properties.joinAsAssignments(".toMutable()")})")
  }
}

internal fun FileCompilerScope.addCopyClosure() {
  addCopyFunction {
    addParameter(name = "block", type = mutable.parameterized.asReceiverConsumer())
    addReturn("toMutable().apply(block).freeze()")
  }
}

private fun FileCompilerScope.addRetrofittedCopyFunction() {
  addCopyFunction {
    properties.forEach { property ->
      addParameter(
        ParameterSpec.builder(name = property.name, type = property.typeName)
          .defaultValue("this.${property.name}").build()
      )
    }
    addReturn(sealedTypes.joinWithWhen { type ->
      "is ${type.name} -> this.copy(${properties.joinToString { "${it.name} = ${it.name}" }})"
    })
  }
}

internal fun FileCompilerScope.addCopyFunction(block: FunSpec.Builder.() -> Unit) {
  addInlinedFunction(name = "copy", receives = target.parameterized, returns = target.parameterized, block = block)
}
