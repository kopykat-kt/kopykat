package fp.serrano.kopykat.utils

import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeVariableName
import fp.serrano.kopykat.parameterizedWhenNotEmpty
import fp.serrano.kopykat.utils.kotlin.poet.className

internal sealed interface TypeCompileScope : KSClassDeclaration {

  val mutableCandidates: Sequence<KSClassDeclaration>
  val logger: KSPLogger

  val targetTypeName get() = simpleName.asString()
  val typeVariableNames get() = typeParameters.map { it.toTypeVariableName() }
  val mutableTypeName get() = "Mutable$targetTypeName"
  val mutableClassName get() = className(mutableTypeName)
  val mutableParameterized get() = mutableClassName.parameterizedWhenNotEmpty(typeVariableNames)
  val properties get() = getAllProperties()
  val typeParamResolver get() = typeParameters.toTypeParameterResolver()
  val targetClassName get() = className(targetTypeName).parameterizedWhenNotEmpty(typeVariableNames)

  fun KSType.hasMutableCopy(): Boolean = declaration.closestClassDeclaration() in mutableCandidates
  fun KSPropertyDeclaration.hasMutableCopy(): Boolean = type.resolve().hasMutableCopy()
  fun KSPropertyDeclaration.toAssignment(mutablePostfix: String, source: String? = null): String =
    "$name = ${source ?: ""}$name${mutablePostfix.takeIf { hasMutableCopy() } ?: ""}"

  fun toFileScope(file: FileSpec.Builder): FileCompilerScope

}

internal class ClassCompileScope(
  private val classDeclaration: KSClassDeclaration,
  override val mutableCandidates: Sequence<KSClassDeclaration>,
  override val logger: KSPLogger,
) : TypeCompileScope, KSClassDeclaration by classDeclaration {
  override fun toFileScope(file: FileSpec.Builder): FileCompilerScope =
    FileCompilerScope(this, file = file)
}

internal class FileCompilerScope(
  parent: TypeCompileScope,
  val file: FileSpec.Builder,
) : TypeCompileScope by parent {
  override fun toFileScope(file: FileSpec.Builder) = this
}

internal fun <R> KSClassDeclaration.onClassScope(
  mutableCandidates: Sequence<KSClassDeclaration>,
  logger: KSPLogger,
  block: TypeCompileScope.() -> R,
): R =
  ClassCompileScope(this, mutableCandidates, logger).block()
