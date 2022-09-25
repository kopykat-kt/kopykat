package at.kopyk

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ksp.toClassName
import at.kopyk.poet.addClass
import at.kopyk.poet.addMutableProperty
import at.kopyk.poet.addParameter
import at.kopyk.poet.addProperty
import at.kopyk.poet.addReturn
import at.kopyk.poet.asReceiverConsumer
import at.kopyk.poet.parameterModifiers
import at.kopyk.poet.primaryConstructor
import at.kopyk.poet.propertyModifiers
import at.kopyk.utils.FileCompilerScope
import at.kopyk.utils.TypeCategory.Known.Data
import at.kopyk.utils.TypeCategory.Known.Sealed
import at.kopyk.utils.TypeCategory.Known.Value
import at.kopyk.utils.TypeCompileScope
import at.kopyk.utils.addGeneratedMarker
import at.kopyk.utils.baseName
import at.kopyk.utils.dslMarker
import at.kopyk.utils.fullName
import at.kopyk.utils.lang.forEachRun
import at.kopyk.utils.lang.joinWithWhen
import at.kopyk.utils.mutable
import at.kopyk.utils.onKnownCategory
import at.kopyk.utils.sealedTypes
import at.kopyk.utils.typeCategory

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
            "is ${it.fullName} -> old.copy(${properties.joinAsAssignments(".freeze()")})"
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
      "is ${type.fullName} -> this.copy(${properties.joinToString { "${it.baseName} = ${it.baseName}" }})"
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
