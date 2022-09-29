package at.kopyk.utils

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSReferenceElement
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.Variance

internal typealias TypeSubstitution = Map<KSName, KSType?>

/* This function needs to "jump" across type aliases
   to get the type parameters applied to the "real" type */
internal fun KSTypeAlias.unravelTypeParameters(): TypeSubstitution {
  val ty = type.resolve()
  return when (val decl = ty.declaration) {
    is KSClassDeclaration ->
      decl.typeParameters.map { it.name }.zip(ty.arguments.map { it.type?.resolve() }).toMap()
    // implement for type aliases referencing other aliases
    else -> emptyMap()
  }
}

internal fun KSType.substitute(subst: TypeSubstitution): KSType =
  when (val decl = declaration) {
    is KSTypeParameter -> subst[decl.name] ?: this
    else -> replace(arguments.map { it.substitute(subst) })
  }

internal fun KSTypeArgument.substitute(subst: TypeSubstitution): KSTypeArgument {
  val previous: KSTypeArgument = this
  return object : KSTypeArgument by previous {
    override val variance = Variance.INVARIANT
    override val type = previous.type?.substitute(subst)
  }
}

internal fun KSTypeReference.substitute(subst: TypeSubstitution): KSTypeReference {
  val previous: KSTypeReference = this
  return object : KSTypeReference by previous {
    override val element: KSReferenceElement? = null
    override fun resolve(): KSType = previous.resolve().substitute(subst)
  }
}
