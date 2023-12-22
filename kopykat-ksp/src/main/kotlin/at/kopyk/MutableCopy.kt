package at.kopyk

import at.kopyk.poet.addClass
import at.kopyk.poet.addMutableProperty
import at.kopyk.poet.addParameter
import at.kopyk.poet.addProperty
import at.kopyk.poet.addReturn
import at.kopyk.poet.asReceiverConsumer
import at.kopyk.poet.makeInvariant
import at.kopyk.poet.parameterModifiers
import at.kopyk.poet.primaryConstructor
import at.kopyk.poet.propertyModifiers
import at.kopyk.utils.FileCompilerScope
import at.kopyk.utils.TypeCategory.Known
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
import at.kopyk.utils.sanitizedName
import at.kopyk.utils.typeCategory
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toKModifier

internal val TypeCompileScope.mutableCopyKt: FileSpec
  get() =
    buildFile(target.mutable.reflectionName()) {
      addImports()
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

internal fun FileCompilerScope.addImports() {
  with(element) {
    val targetPkg = target.packageName
    mutationInfo.forEach { (declaration, mutationInfo) ->
      if (declaration.type.resolve().hasMutableCopy()) {
        mutationInfo.className.usedPackages.forEach { propertyPkg ->
          if (propertyPkg != targetPkg) {
            file.addImport(propertyPkg, "freeze", "toMutable")
          }
        }
      }
    }
  }
}

@Suppress("RecursivePropertyAccessor")
internal val TypeName.usedPackages: Set<String>
  get() =
    when (this) {
      is ClassName -> setOf(this.packageName)
      is ParameterizedTypeName ->
        this.rawType.usedPackages + this.typeArguments.flatMap { it.usedPackages }
      else -> emptySet()
    }

internal fun FileCompilerScope.addMutableCopy() {
  with(element) {
    file.addClass(target.mutable) {
      visibility.toKModifier()?.let { addModifiers(it) }
      addAnnotation(target.dslMarker)
      addTypeVariables(typeVariableNames.map { it.makeInvariant() })
      primaryConstructor {
        mutationInfo.forEach { (property, mutationInfo) ->
          with(property) {
            addParameter(
              name = baseName,
              type = mutationInfo.className,
              modifiers = parameterModifiers,
            )
            addMutableProperty(
              name = baseName,
              type = mutationInfo.className,
              modifiers = propertyModifiers,
              initializer = baseName,
            )
          }
        }
        addParameter(name = "old", type = target.parameterized)
        addProperty(name = "old", type = target.parameterized, initializer = "old")
      }
    }
  }
}

internal fun FileCompilerScope.addFreezeFunction() {
  with(element) {
    onKnownCategory { category ->
      addFunction(name = "freeze", receives = target.mutable.parameterized, returns = target.parameterized) {
        visibility.toKModifier()?.let { addModifiers(it) }
        addReturn(
          when (category) {
            Known.Class -> error("Plain classes are not supported as mutable")
            Data, Value -> "${target.canonicalName}(${mutationInfo.joinAsAssignmentsWithMutation { freeze(it) }})"
            Sealed ->
              sealedTypes.joinWithWhen(subject = "old") { type ->
                "is ${type.fullName} -> old.copy(${mutationInfo.joinAsAssignmentsWithMutation { freeze(it) }})"
              }
          },
        )
      }
    }
  }
}

internal fun FileCompilerScope.addToMutateFunction() {
  with(element) {
    val parameterized = target.mutable.parameterized
    addFunction(name = "toMutable", receives = target.parameterized, returns = parameterized) {
      visibility.toKModifier()?.let { addModifiers(it) }
      addReturn("$parameterized(old = this, ${mutationInfo.joinAsAssignmentsWithMutation { toMutable(it) }})")
    }
  }
}

internal fun FileCompilerScope.addCopyClosure() {
  with(element) {
    val parameterized = target.mutable.parameterized
    addCopyFunction {
      addParameter(name = "block", type = parameterized.asReceiverConsumer())
      addReturn("toMutable().apply(block).freeze()")
    }
  }
}

private fun FileCompilerScope.addRetrofittedCopyFunction() {
  with(element) {
    addCopyFunction {
      properties.forEachRun { addParameter(name = baseName, type = typeName, defaultValue = "this.$sanitizedName") }
      addReturn(
        sealedTypes.joinWithWhen { type ->
          "is ${type.fullName} -> this.copy(${properties.joinToString { "${it.sanitizedName} = ${it.sanitizedName}" }})"
        },
      )
    }
  }
}

internal fun FileCompilerScope.addCopyFunction(block: FunSpec.Builder.() -> Unit) {
  with(element) {
    addInlinedFunction(name = "copy", receives = target.parameterized, returns = target.parameterized) {
      visibility.toKModifier()?.let { addModifiers(it) }
      block()
    }
  }
}

private fun FileCompilerScope.addDslMarkerClass() {
  file.addClass(element.target.dslMarker) {
    addAnnotation(DslMarker::class)
    addModifiers(KModifier.ANNOTATION)
  }
}
