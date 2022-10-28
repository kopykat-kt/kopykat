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
    yieldAll(copyFromTargets.map { CopyPair(self, it, self) })
    yieldAll(copyToTargets.map { CopyPair(self, self, it) })
  }

internal val CopyPair.fileName get() = fileName(from, to)

internal fun fileName(from: KSClassDeclaration, to: KSClassDeclaration) =
  from.className.append(to.baseName).reflectionName()

internal fun fileSpec(
  others: Sequence<CopyPair>,
  copyPair: CopyPair,
): FileSpec? = copyPair.let { (self, from, to) ->
  with(self) {
    when {
      from.isIsomorphicOf(others, to) -> buildFile(fileName = copyPair.fileName) {
        addGeneratedMarker()
        addInlinedFunction(name = to.baseName, receives = null, returns = to.className.parameterized) {
          addParameter(name = "from", type = from.className)
          val fromProperties = from.asScope().properties
          val toProperties = to.asScope().properties
          fromProperties.zip(toProperties, ::propertyDefinition.partially1(others))
            .filterNotNull()
            .run { addReturn("${to.baseName}(${joinToString()})") }
        }
      }

      else -> {
        val message = "${to.fullName} must have the same constructor properties as ${from.fullName}"
        logger.error(message = message, symbol = self)
        null
      }
    }
  }
}

private fun propertyDefinition(
  others: Sequence<CopyPair>,
  fromProperty: KSPropertyDeclaration,
  toProperty: KSPropertyDeclaration,
): String? = zip(
  fromProperty.asClassDeclaration(),
  toProperty.asClassDeclaration(),
) { fromType, toType ->
  val propertyName = fromProperty.baseName
  when {
    others.hasCopyConstructor(fromType, toType) -> "$propertyName = ${toType.baseName}(from.$propertyName)"
    else -> "$propertyName = from.$propertyName"
  }
}

private fun KSPropertyDeclaration.asClassDeclaration() =
  type.resolve().declaration.takeIfInstanceOf<KSClassDeclaration>()

private fun KSClassDeclaration.isIsomorphicOf(
  copies: Sequence<CopyPair>,
  other: KSClassDeclaration,
): Boolean {
  val properties = getAllProperties().toSet()
  val otherProperties = other.getPrimaryConstructorProperties().toSet()
  if (properties.size != otherProperties.size) return false
  if (properties.names != otherProperties.names) return false
  otherProperties.zip(properties) { property, otherProperty ->
    if (!property.isCopiableTo(copies, otherProperty)) return false
  }
  return true
}

private val Iterable<KSPropertyDeclaration>.names get() = map { it.baseName }

private fun KSPropertyDeclaration.isCopiableTo(
  copies: Sequence<CopyPair>,
  other: KSPropertyDeclaration,
): Boolean {
  val from = type.resolve()
  val to = other.type.resolve()
  val hasCopyConstructor = zip(
    from.declaration as? KSClassDeclaration,
    to.declaration as? KSClassDeclaration,
    copies::hasCopyConstructor,
  ) == true
  return when {
    hasCopyConstructor -> true
    from.isAssignableFrom(to) -> true
    else -> false
  }
}

private fun Sequence<CopyPair>.hasCopyConstructor(
  from: KSClassDeclaration,
  to: KSClassDeclaration,
): Boolean {
  val name = fileName(from, to)
  return any { it.fileName == name }
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
