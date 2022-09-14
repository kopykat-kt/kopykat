package fp.serrano.kopykat.utils

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeVariableName
import fp.serrano.kopykat.parameterizedWhenNotEmpty

@JvmInline
internal value class ClassScope(private val classDeclaration: KSClassDeclaration) {
  val packageName get() = classDeclaration.packageName.asString()
  val targetTypeName get() = classDeclaration.simpleName.asString()
  val typeVariableNames get() = classDeclaration.typeParameters.map { it.toTypeVariableName() }
  val kopyKatFileName get() = "${targetTypeName}KopyKat"
  val annotationTypeName get() = "${targetTypeName}CopyDslMarker"
  val annotationClassName get() = ClassName(packageName, annotationTypeName)
  val mutableTypeName get() = "Mutable$targetTypeName"
  val mutableClassName get() = ClassName(packageName, mutableTypeName)
  val mutableParameterized get() = mutableClassName.parameterizedWhenNotEmpty(typeVariableNames)
  val properties get() = classDeclaration.getAllProperties()
  val typeParamResolver get() = classDeclaration.typeParameters.toTypeParameterResolver()
  val targetClassName get() = ClassName(packageName, targetTypeName).parameterizedWhenNotEmpty(typeVariableNames)
}

internal fun <R> KSClassDeclaration.onClassScope(block: ClassScope.() -> R): R =
  ClassScope(this).block()
