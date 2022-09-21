@file:Suppress("WildcardImport")

package fp.serrano.kopykat

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier.ANNOTATION
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.ksp.toTypeName
import fp.serrano.kopykat.utils.ClassScope
import fp.serrano.kopykat.utils.addGeneratedMarker
import fp.serrano.kopykat.utils.name

internal fun ClassScope.mutableCopyKt(): FileSpec =
  buildFile(packageName = packageName.asString(), fileName = mutableTypeName) {
    addGeneratedMarker()
    addClass(annotationClassName) {
      addAnnotation(DslMarker::class)
      addModifiers(ANNOTATION)
    }
    addClass(mutableClassName) {
      addAnnotation(annotationClassName)
      addTypeVariables(typeVariableNames)
      primaryConstructor {
        properties.forEach { property ->
          val originalName = property.type.toTypeName(typeParamResolver)
          val type = property.type.resolve()
          val declaration = type.declaration

          val typeName = type.takeIf { it.hasMutableCopy() }
            ?.let { ClassName(declaration.packageName.asString(), "Mutable${declaration.name}") }
            ?: originalName

          addParameter(property.asParameterSpec(typeName))
          addProperty(property.asPropertySpec(typeName) {
            mutable(true).initializer(property.name)
          })
        }
        addParameter(name = "old", type = targetClassName)
        addProperty(PropertySpec.builder(name = "old", type = targetClassName).initializer("old").build())
      }
    }
    addFunction(
      name = "freeze",
      receiver = mutableParameterized,
      returns = targetClassName,
      typeVariables = typeVariableNames,
      inlined = false,
    ) {
      val assignments = properties.map { it.toAssignment(".freeze()") }
      addReturn("$targetTypeName(${assignments.joinToString()})")
    }

    addFunction(
      name = "toMutable",
      receiver = targetClassName,
      returns = mutableParameterized,
      typeVariables = typeVariableNames,
      inlined = false,
    ) {
      val assignments = properties.map { it.toAssignment(".toMutable()") } + "old = this"
      addReturn("$mutableParameterized(${assignments.joinToString()})")
    }

    addFunction(
      name = "copy",
      receiver = targetClassName,
      returns = targetClassName,
      typeVariables = typeVariableNames,
    ) {
      addParameter(name = "block", type = LambdaTypeName.get(receiver = mutableParameterized, returnType = UNIT))
      addReturn("toMutable().apply(block).freeze()")
    }
  }
