package at.kopyk

import at.kopyk.poet.addReturn
import at.kopyk.poet.append
import at.kopyk.utils.ClassCompileScope
import at.kopyk.utils.addGeneratedMarker
import at.kopyk.utils.baseName
import at.kopyk.utils.getPrimaryConstructorProperties
import at.kopyk.utils.lang.mapRun
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ksp.toTypeName

internal val ClassCompileScope.copyFromParentKt: FileSpec
  get() = buildFile(fileName = target.append("FromParent").reflectionName()) {
    val parameterized = target.parameterized
    addGeneratedMarker()

    parentTypes
      .filter { it.containsAllPropertiesOf(classDeclaration) }
      .forEach { parent ->
        addInlinedFunction(name = target.simpleName, receives = null, returns = parameterized) {
          addParameter(
            name = "from",
            type = parent.toTypeName(typeParameterResolver)
          )
          properties
            .mapRun { "$baseName = from.$baseName" }
            .run { addReturn("${target.simpleName}(${joinToString()})") }
        }
      }
  }

internal fun KSType.containsAllPropertiesOf(child: KSClassDeclaration): Boolean =
  child.getPrimaryConstructorProperties().all { childProperty ->
    (this.declaration as? KSClassDeclaration)?.getAllProperties().orEmpty().any { parentProperty ->
      childProperty.baseName == parentProperty.baseName &&
        childProperty.type.resolve().isAssignableFrom(parentProperty.type.resolve())
    }
  }
