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
