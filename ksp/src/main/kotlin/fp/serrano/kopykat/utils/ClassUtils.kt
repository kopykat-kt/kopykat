@file:Suppress("WildcardImport")
package fp.serrano.kopykat.utils

import com.google.devtools.ksp.*
import com.google.devtools.ksp.symbol.*

internal fun KSClassDeclaration.isDataClass() =
  Modifier.DATA in modifiers && primaryConstructor != null

internal fun KSClassDeclaration.isValueClass() =
  Modifier.VALUE in modifiers && primaryConstructor != null

internal fun KSClassDeclaration.isSealedDataHierarchy() =
  Modifier.SEALED in modifiers
          && isAbstract()
          && getSealedSubclasses().all { it.isDataClass() }
