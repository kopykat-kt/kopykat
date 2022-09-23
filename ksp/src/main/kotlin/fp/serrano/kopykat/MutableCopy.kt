@file:Suppress("WildcardImport")

package fp.serrano.kopykat

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ksp.toClassName
import fp.serrano.kopykat.utils.*
import fp.serrano.kopykat.utils.FileCompilerScope
import fp.serrano.kopykat.utils.TypeCategory.Known.Data
import fp.serrano.kopykat.utils.TypeCategory.Known.Sealed
import fp.serrano.kopykat.utils.TypeCategory.Known.Value
import fp.serrano.kopykat.utils.TypeCompileScope
import fp.serrano.kopykat.utils.addGeneratedMarker
import fp.serrano.kopykat.utils.lang.forEachRun
import fp.serrano.kopykat.utils.lang.joinWithWhen
import fp.serrano.kopykat.utils.onKnownCategory
import fp.serrano.kopykat.utils.sealedTypes
import fp.serrano.kopykat.utils.typeCategory

internal val TypeCompileScope.mutableCopyKt: FileSpec
  get() = buildFile(target.mutable.reflectionName()) {
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
  file.addClass(target.mutable) {
    addAnnotation(target.dslMarker)
    addTypeVariables(typeVariableNames)
    primaryConstructor {
      properties.forEachRun {
        val typeName = type.resolve().takeIf { it.hasMutableCopy() }?.toClassName()?.mutable ?: typeName
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
    addFunction(name = "freeze", receives = target.mutable.parameterized, returns = target.parameterized) {
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
  val parameterized = target.mutable.parameterized
  addFunction(name = "toMutable", receives = target.parameterized, returns = parameterized) {
    addReturn("$parameterized(old = this, ${properties.joinAsAssignments(".toMutable()")})")
  }
}

internal fun FileCompilerScope.addCopyClosure() {
  val parameterized = target.mutable.parameterized
  addCopyFunction {
    addParameter(name = "block", type = parameterized.asReceiverConsumer())
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

private fun FileCompilerScope.addDslMarkerClass() {
  file.addClass(target.dslMarker) {
    addAnnotation(DslMarker::class)
    addModifiers(KModifier.ANNOTATION)
  }
}
