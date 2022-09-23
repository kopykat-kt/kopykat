@file:Suppress("WildcardImport")

package fp.serrano.kopykat

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ksp.toClassName
import fp.serrano.kopykat.utils.*
import fp.serrano.kopykat.utils.FileCompilerScope
import fp.serrano.kopykat.utils.TypeCategory.Known.Data
import fp.serrano.kopykat.utils.TypeCategory.Known.Sealed
import fp.serrano.kopykat.utils.TypeCategory.Known.Value
import fp.serrano.kopykat.utils.TypeCompileScope
import fp.serrano.kopykat.utils.addDslMarkerClass
import fp.serrano.kopykat.utils.addGeneratedMarker
import fp.serrano.kopykat.utils.annotationClassName
import fp.serrano.kopykat.utils.lang.forEachRun
import fp.serrano.kopykat.utils.lang.joinWithWhen
import fp.serrano.kopykat.utils.onKnownCategory
import fp.serrano.kopykat.utils.sealedTypes
import fp.serrano.kopykat.utils.typeCategory

internal val TypeCompileScope.mutableCopyKt: FileSpec
  get() = buildFile(mutable.reflectionName()) {
    addGeneratedMarker()
    addDslMarkerClass()
    addMutableCopy()
    addFreezeFunction()
    addToMutateFunction()
    addCopyClosure()

    if (typeCategory is Sealed) {
      addRetrofittedCopyFunction()
    }
  }

internal fun FileCompilerScope.addMutableCopy() {
  file.addClass(mutable) {
    addAnnotation(annotationClassName)
    addTypeVariables(typeVariableNames)
    primaryConstructor {
      properties.forEachRun {
        val typeName = type.resolve().takeIf { it.hasMutableCopy() }?.toClassName()?.map { "Mutable$it" } ?: typeName
        addParameter(name = baseName, type = typeName, modifiers = parameterModifiers)
        addMutableProperty(name = baseName, type = typeName, modifiers = propertyModifiers, initializer = baseName)
      }
      addParameter(name = "old", type = target.parameterized)
      addProperty(name = "old", type = target.parameterized, initializer = "old")
    }
  }
}

internal fun FileCompilerScope.addFreezeFunction() {
  onKnownCategory { category ->
    addFunction(name = "freeze", receives = mutable.parameterized, returns = target.parameterized) {
      addReturn(
        when (category) {
          Data, Value -> "${target.canonicalName}(${properties.joinAsAssignments(".freeze()")})"
          Sealed -> sealedTypes.joinWithWhen(subject = "old") {
            "is ${it.qfName} -> old.copy(${properties.joinAsAssignments(".freeze()")})"
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
    properties.forEachRun { addParameter(name = baseName, type = typeName, defaultValue = "this.$baseName") }
    addReturn(sealedTypes.joinWithWhen { type ->
      "is ${type.qfName} -> this.copy(${properties.joinToString { "${it.baseName} = ${it.baseName}" }})"
    })
  }
}

internal fun FileCompilerScope.addCopyFunction(block: FunSpec.Builder.() -> Unit) {
  addInlinedFunction(name = "copy", receives = target.parameterized, returns = target.parameterized, block = block)
}
