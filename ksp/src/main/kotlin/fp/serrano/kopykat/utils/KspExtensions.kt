package fp.serrano.kopykat.utils

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration

internal val KSDeclaration.name get() = simpleName.asString()

internal val KSClassDeclaration.sealedTypes get() = getSealedSubclasses()
internal fun <R> KSClassDeclaration.mapSealedSubclasses(transform: (KSClassDeclaration) -> R): Sequence<R> =
  sealedTypes.map(transform)
