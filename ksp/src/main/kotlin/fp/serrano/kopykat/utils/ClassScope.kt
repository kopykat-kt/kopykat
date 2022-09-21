package fp.serrano.kopykat.utils

import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeVariableName
import fp.serrano.kopykat.parameterizedWhenNotEmpty

internal class ClassScope(
  private val classDeclaration: KSClassDeclaration,
  private val mutableCandidates: Sequence<KSClassDeclaration>,
): KSClassDeclaration by classDeclaration {
  val targetTypeName get() = classDeclaration.simpleName.asString()
  val typeVariableNames get() = classDeclaration.typeParameters.map { it.toTypeVariableName() }
  val kopyKatFileName get() = "${targetTypeName}KopyKat"
  val kopyKatCopyFileName get() = "${targetTypeName}CopyKopyKat"
  val kopyKatCopyMapFileName get() = "${targetTypeName}CopyMapKopyKat"
  val annotationTypeName get() = "${targetTypeName}CopyDslMarker"
  val annotationClassName get() = ClassName(packageName.asString(), annotationTypeName)
  val mutableTypeName get() = "Mutable$targetTypeName"
  val mutableClassName get() = ClassName(packageName.asString(), mutableTypeName)
  val mutableParameterized get() = mutableClassName.parameterizedWhenNotEmpty(typeVariableNames)
  val properties get() = classDeclaration.getAllProperties()
  val typeParamResolver get() = classDeclaration.typeParameters.toTypeParameterResolver()
  val targetClassName get() = ClassName(packageName.asString(), targetTypeName).parameterizedWhenNotEmpty(typeVariableNames)

  fun KSType.hasMutableCopy(): Boolean = declaration.closestClassDeclaration() in mutableCandidates
  fun KSPropertyDeclaration.hasMutableCopy(): Boolean = type.resolve().hasMutableCopy()
  fun KSPropertyDeclaration.toAssignment(mutablePostfix: String, source: String? = null): String =
    "$name = ${source ?: ""}$name${mutablePostfix.takeIf { hasMutableCopy() } ?: ""}"

}

internal fun <R> KSClassDeclaration.onClassScope(
  mutableCandidates: Sequence<KSClassDeclaration>,
  block: ClassScope.() -> R,
): R =
  ClassScope(this, mutableCandidates).block()
