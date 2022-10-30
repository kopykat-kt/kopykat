package at.kopyk

import arrow.core.Nullable.zip
import arrow.core.partially1
import at.kopyk.poet.addReturn
import at.kopyk.poet.append
import at.kopyk.poet.className
import at.kopyk.utils.ClassCompileScope
import at.kopyk.utils.TypeCompileScope
import at.kopyk.utils.addGeneratedMarker
import at.kopyk.utils.baseName
import at.kopyk.utils.fullName
import at.kopyk.utils.getPrimaryConstructorProperties
import at.kopyk.utils.lang.takeIfInstanceOf
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.FileSpec

internal data class CopyPair(
  val self: TypeCompileScope,
  val from: KSClassDeclaration,
  val to: KSClassDeclaration,
)

internal val ClassCompileScope.allCopies: Sequence<CopyPair>
  get() = sequence {
    val copyFromTargets = typesFor<CopyFrom>() + typesFor<Copy>()
    val copyToTargets = typesFor<CopyTo>() + typesFor<Copy>()

    val self = this@allCopies
    val pair = ::CopyPair.partially1(self)
    yieldAll(copyFromTargets.map { other -> pair(other, self) })
    yieldAll(copyToTargets.map { other -> pair(self, other) })
  }

internal val CopyPair.name get() = nameFor(from, to)

internal fun nameFor(from: KSClassDeclaration, to: KSClassDeclaration) =
  from.className.append(to.baseName).reflectionName()

internal fun fileSpec(
  others: Sequence<CopyPair>,
  copyPair: CopyPair,
): FileSpec? =
  with(copyPair) {
    when {
      from.isIsomorphicOf(others, to) -> self.copyConstructorFileSpec(copyPair, others)
      else -> self.reportMismatchedProperties(copyPair)
    }
  }

private fun TypeCompileScope.copyConstructorFileSpec(
  copyPair: CopyPair,
  others: Sequence<CopyPair>
): FileSpec =
  with(copyPair) {
    buildFile(fileName = name) {
      addGeneratedMarker()
      addInlinedFunction(name = to.baseName, receives = null, returns = to.className.parameterized) {
        addParameter(name = "from", type = from.className)
        val propertyAssignments = to.properties.toList().zipByName(from.getAllProperties().toList()) { to, from ->
          propertyDefinition(others, from, to)
        }.filterNotNull()
        addReturn("${to.baseName}(${propertyAssignments.joinToString()})")
      }
    }
  }

private fun TypeCompileScope.reportMismatchedProperties(
  copyPair: CopyPair
): Nothing? = with(copyPair) {
  val message = "${to.fullName} must have the same constructor properties as ${from.fullName}"
  logger.error(message = message, symbol = self)
  null
}

private fun propertyDefinition(
  others: Sequence<CopyPair>,
  from: KSPropertyDeclaration,
  to: KSPropertyDeclaration,
): String? = zip(from.typeDeclaration, to.typeDeclaration) { fromType, toType ->
  val propertyName = from.baseName
  when {
    others.hasCopyConstructor(fromType, toType) -> "$propertyName = ${toType.baseName}(from.$propertyName)"
    else -> "$propertyName = from.$propertyName"
  }
}

private val KSPropertyDeclaration.typeDeclaration
  get() = type.resolve().declaration.takeIfInstanceOf<KSClassDeclaration>()

private fun KSClassDeclaration.isIsomorphicOf(
  copies: Sequence<CopyPair>,
  other: KSClassDeclaration,
): Boolean {
  val properties = getAllProperties().toSet()
  val otherProperties = other.getPrimaryConstructorProperties().toSet()
  if (properties.size != otherProperties.size) return false
  if (properties.names.toSet() != otherProperties.names.toSet()) return false
  otherProperties.zipByName(properties) { property, otherProperty ->
    if (!property.isCopiableTo(copies, otherProperty)) return@isIsomorphicOf false
  }
  return true
}

private val Iterable<KSPropertyDeclaration>.names get() = map { it.baseName }

private fun KSPropertyDeclaration.isCopiableTo(
  copies: Sequence<CopyPair>,
  other: KSPropertyDeclaration,
): Boolean {
  val hasCopyConstructor = zip(typeDeclaration, other.typeDeclaration, copies::hasCopyConstructor) == true
  return hasCopyConstructor || isAssignableFrom(other)
}

private fun KSPropertyDeclaration.isAssignableFrom(other: KSPropertyDeclaration) =
  type.resolve().isAssignableFrom(other.type.resolve())

private fun Sequence<CopyPair>.hasCopyConstructor(from: KSClassDeclaration, to: KSClassDeclaration): Boolean {
  val name = nameFor(from, to)
  return any { it.name == name }
}

internal inline fun <reified A : Annotation> ClassCompileScope.typesFor() =
  annotationsOf<A>().mapNotNull { it.type?.declaration }.filterIsInstance<KSClassDeclaration>()

private val KSAnnotation.type
  get() = arguments.firstOrNull { it.name?.getShortName() == "type" }?.value as? KSType

private inline fun <reified T : Annotation> KSAnnotated.annotationsOf(): Sequence<KSAnnotation> =
  annotations.filter {
    it.shortName.getShortName() == T::class.simpleName && it.annotationType.resolve().declaration
      .qualifiedName?.asString() == T::class.qualifiedName
  }

private inline fun <A> Iterable<KSPropertyDeclaration>.zipByName(
  other: Iterable<KSPropertyDeclaration>,
  transform: (KSPropertyDeclaration, KSPropertyDeclaration) -> A
): List<A> =
  map { thisProp ->
    val otherProp = other.first { otherProp ->
      thisProp.baseName == otherProp.baseName
    }
    transform(thisProp, otherProp)
  }
