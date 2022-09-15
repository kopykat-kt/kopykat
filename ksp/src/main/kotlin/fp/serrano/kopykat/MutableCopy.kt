@file:Suppress("WildcardImport")

package fp.serrano.kopykat

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toTypeName
import fp.serrano.kopykat.utils.addGeneratedMarker
import fp.serrano.kopykat.utils.name
import fp.serrano.kopykat.utils.onClassScope

internal fun KSClassDeclaration.mutableCopyKt(mutableCandidates: Sequence<KSType>): FileSpec = onClassScope {
  buildFile(packageName = packageName, fileName = mutableTypeName) {
    addGeneratedMarker()
    addClass(annotationClassName) {
      addAnnotation(DslMarker::class)
      addModifiers(KModifier.ANNOTATION)
    }
    addClass(mutableClassName) {
      addAnnotation(annotationClassName)
      addTypeVariables(typeVariableNames)
      primaryConstructor {
        properties.forEach { property ->
          val originalName = property.type.toTypeName(typeParamResolver)
          val type = property.type.resolve()
          val declaration = type.declaration

          val typeName = type.takeIf { it in mutableCandidates }
            ?.let { ClassName(declaration.packageName.asString(), "Mutable${declaration.simpleName.asString()}") }
            ?: originalName

          addParameter(property.asParameterSpec(typeName))
          addProperty(property.asPropertySpec(typeName) {
            mutable(true).initializer(property.simpleName.asString())
          })
        }
        addParameter(name = "old", type = targetClassName)
        addProperty(
          PropertySpec.builder(
            name = "old",
            type = targetClassName
          ).mutable(false).initializer("old").build()
        )
      }
    }
    addFunction(
      name = "freeze",
      receiver = mutableClassName,
      returns = targetClassName,
      typeVariables = typeVariableNames,
    ) {
      addModifiers(KModifier.INLINE)
      val assignments = properties.map {
        if(it.type.resolve() in mutableCandidates) {
          "${it.name} = ${it.name}.freeze()"
        } else {
          "${it.name} = ${it.name}"
        }
      }
      addCode("return $targetTypeName(${assignments.joinToString()})")
    }
    addFunction(
      name = "toMutable",
      receiver = targetClassName,
      returns = mutableClassName,
      typeVariables = typeVariableNames,
    ) {
      addModifiers(KModifier.INLINE)
      val assignments = properties.map {
        if(it.type.resolve() in mutableCandidates) {
          "${it.name} = ${it.name}.toMutable()"
        } else {
          "${it.name} = ${it.name}"
        }
      } + "old = this"
      addCode("return $mutableParameterized(${assignments.joinToString()})")
    }
    addFunction(
      name = "copy",
      receiver = targetClassName,
      returns = targetClassName,
      typeVariables = typeVariableNames,
    ) {
      addModifiers(KModifier.INLINE)
      addParameter(name = "block", type = LambdaTypeName.get(receiver = mutableParameterized, returnType = UNIT))
      val mutableAssignments = properties.map {
        if(it.type.resolve() in mutableCandidates) {
          "${it.name} = ${it.name}.toMutable()"
        } else {
          "${it.name} = ${it.name}"
        }
      } + "old = this"

      val compileAssignment = properties.map {
        if(it.type.resolve() in mutableCandidates) {
          "${it.name} = mutable.${it.name}.freeze()"
        } else {
          "${it.name} = mutable.${it.name}"
        }
      }

      addCode(
        """
        | val mutable = $mutableParameterized(${mutableAssignments.joinToString()}).apply(block)
        | return $targetClassName(${compileAssignment.joinToString()})
        """.trimMargin()
      )
    }
  }
}
