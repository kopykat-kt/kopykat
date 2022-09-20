@file:Suppress("WildcardImport")

package fp.serrano.kopykat

import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.KModifier.ANNOTATION
import com.squareup.kotlinpoet.ksp.toTypeName
import fp.serrano.kopykat.utils.addGeneratedMarker
import fp.serrano.kopykat.utils.name
import fp.serrano.kopykat.utils.onClassScope

internal fun KSClassDeclaration.mutableCopyKt(
  mutableCandidates: Sequence<KSClassDeclaration>,
): FileSpec = onClassScope {

  fun KSType.hasMutableCopy(): Boolean = declaration.closestClassDeclaration() in mutableCandidates
  fun KSPropertyDeclaration.hasMutableCopy(): Boolean = type.resolve().hasMutableCopy()
  fun KSPropertyDeclaration.toAssignment(mutablePostfix: String, source: String? = null): String =
    "$name = ${source ?: ""}$name${mutablePostfix.takeIf { hasMutableCopy() } ?: ""}"

  buildFile(packageName = packageName, fileName = mutableTypeName) {
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
      receiver = mutableParameterized,
      returns = targetClassName,
      typeVariables = typeVariableNames,
      inlined = false,
    ) {
      val assignments = properties.map { it.toAssignment(".freeze()") }
      addCode("return $targetTypeName(${assignments.joinToString()})")
    }

    addFunction(
      name = "toMutable",
      receiver = targetClassName,
      returns = mutableParameterized,
      typeVariables = typeVariableNames,
      inlined = false,
    ) {
      val assignments = properties.map { it.toAssignment(".toMutable()") } + "old = this"
      addCode("return $mutableParameterized(${assignments.joinToString()})")
    }
    addFunction(
      name = "copy",
      receiver = targetClassName,
      returns = targetClassName,
      typeVariables = typeVariableNames,
    ) {
      addParameter(name = "block", type = LambdaTypeName.get(receiver = mutableParameterized, returnType = UNIT))
      val mutableAssignments = properties.map { it.toAssignment(".toMutable()") } + "old = this"

      val compileAssignment = properties.map { it.toAssignment(mutablePostfix = ".freeze()", source = "mutable.") }

      addCode(
        """
        | val mutable = $mutableParameterized(${mutableAssignments.joinToString()}).apply(block)
        | return $targetClassName(${compileAssignment.joinToString()})
        """.trimMargin()
      )
    }
  }
}
