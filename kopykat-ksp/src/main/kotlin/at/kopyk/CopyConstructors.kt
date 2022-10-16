package at.kopyk

import at.kopyk.poet.addReturn
import at.kopyk.poet.append
import at.kopyk.poet.className
import at.kopyk.utils.ClassCompileScope
import at.kopyk.utils.TypeCompileScope
import at.kopyk.utils.addGeneratedMarker
import at.kopyk.utils.baseName
import at.kopyk.utils.fullName
import at.kopyk.utils.getPrimaryConstructorProperties
import at.kopyk.utils.lang.mapRun
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.FileSpec


internal fun ClassCompileScope.copyConstructorsKt(): Sequence<FileSpec> =
  sequence {
    val copyTargets = typesFor<Copy>()
    val copyFromTargets = typesFor<CopyFrom>() + copyTargets
    val copyToTargets = typesFor<CopyTo>() + copyTargets

    val self = this@copyConstructorsKt
    yieldAll(copyFromTargets.mapNotNull { from -> copyConstructor(from = from, to = self) })
    yieldAll(copyToTargets.mapNotNull { to -> copyConstructor(from = self, to = to) })
  }

private fun TypeCompileScope.copyConstructor(from: KSClassDeclaration, to: KSClassDeclaration): FileSpec? =
  if (from.isIsomorphicOf(to)) {
    buildFile(fileName = target.append("_${to.baseName}").reflectionName()) {
      addGeneratedMarker()
      addInlinedFunction(name = to.baseName, receives = null, returns = to.className.parameterized) {
        addParameter(name = "from", type = from.className)
        properties
          .mapRun { "$baseName = from.$baseName" }
          .run { addReturn("${target.simpleName}(${joinToString()})") }
      }
    }
  } else {
    val message = "${target.simpleName} must have the same constructor properties as ${from.fullName}"
    logger.error(message = message, symbol = this@copyConstructor)
    null
  }

private fun KSClassDeclaration.isIsomorphicOf(other: KSClassDeclaration): Boolean {
  val properties = getAllProperties().toSet()
  val otherProperties = other.getPrimaryConstructorProperties().toSet()
  if (properties.size != otherProperties.size) return false
  if (properties.names != otherProperties.names) return false
  otherProperties.zip(properties) { property, otherProperty ->
    if (!property.isAssignableFrom(otherProperty)) return false
  }
  return true
}

private val Iterable<KSPropertyDeclaration>.names get() = map { it.baseName }

private fun KSPropertyDeclaration.isAssignableFrom(other: KSPropertyDeclaration): Boolean =
  type.resolve().isAssignableFrom(other.type.resolve())

internal inline fun <reified T : Annotation> ClassCompileScope.typesFor() =
  annotationsOf<T>().mapNotNull { it.type?.declaration }.filterIsInstance<KSClassDeclaration>()

private val KSAnnotation.type
  get() = arguments.firstOrNull { it.name?.getShortName() == "type" }?.value as? KSType


private inline fun <reified T : Annotation> KSAnnotated.annotationsOf(): Sequence<KSAnnotation> =
  annotations.filter {
    it.shortName.getShortName() == T::class.simpleName && it.annotationType.resolve().declaration
      .qualifiedName?.asString() == T::class.qualifiedName
  }