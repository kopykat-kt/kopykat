package fp.serrano.kopykat.utils

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSDeclarationContainer
import com.google.devtools.ksp.symbol.KSPropertyDeclaration

internal val KSDeclaration.baseName get() = simpleName.asString()

internal val KSDeclaration.qfName get() = qualifiedName?.asString() ?: simpleName.asString()

internal val KSClassDeclaration.sealedTypes get() = getSealedSubclasses()

internal fun KSDeclarationContainer.allNestedDeclarations(): Sequence<KSDeclaration> =
  declarations.flatMap {
    sequence {
      yield(it)
      if (it is KSDeclarationContainer)
        yieldAll(it.allNestedDeclarations())
    }
  }

/**
 * Obtains those properties which are defined in the primary constructor,
 * or in every primary constructor of their children.
 */
internal fun KSClassDeclaration.getPrimaryConstructorProperties() =
  getAllProperties().filter { property ->
    hasPrimaryProperty(property) || (sealedTypes.any() && sealedTypes.all { it.hasPrimaryProperty(property) })
  }

private fun KSClassDeclaration.hasPrimaryProperty(property: KSPropertyDeclaration) =
  primaryConstructor?.parameters.orEmpty().any { param ->
    (param.isVal || param.isVar) && param.name?.asString() == property.baseName
  }
