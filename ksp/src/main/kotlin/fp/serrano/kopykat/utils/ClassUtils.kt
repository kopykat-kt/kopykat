@file:Suppress("WildcardImport")
package fp.serrano.kopykat.utils

import com.google.devtools.ksp.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.symbol.Modifier.DATA
import com.google.devtools.ksp.symbol.Modifier.SEALED
import com.google.devtools.ksp.symbol.Modifier.VALUE

internal fun KSClassDeclaration.isConstructable() = primaryConstructor?.isPublic() == true

internal fun KSClassDeclaration.isDataClass() = isConstructable() && DATA in modifiers

internal fun KSClassDeclaration.isValueClass() = isConstructable() && VALUE in modifiers

internal fun KSClassDeclaration.isSealedDataHierarchy() =
  SEALED in modifiers && isAbstract() && hasOnlyDataClassChildren()

private fun KSClassDeclaration.hasOnlyDataClassChildren() =
  sealedTypes.all { it.isDataClass() || it.isValueClass() }

/**
 * Obtains those properties which are defined in the primary constructor,
 * or in every primary constructor of their children.
 */
internal fun KSClassDeclaration.getPrimaryConstructorProperties() =
  getAllProperties().filter { property ->
    hasPrimaryProperty(property) || (sealedTypes.any() && sealedTypes.all { it.hasPrimaryProperty(property) })
  }

internal fun KSClassDeclaration.hasPrimaryProperty(property: KSPropertyDeclaration) =
  primaryConstructor?.parameters.orEmpty().any { param ->
    (param.isVal || param.isVar) && param.name?.asString() == property.name
  }
