package fp.serrano.kopykat.utils

import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeVariableName
import fp.serrano.kopykat.parameterizedWhenNotEmpty
import fp.serrano.kopykat.utils.kotlin.poet.className
import fp.serrano.kopykat.utils.kotlin.poet.map

internal sealed interface TypeCompileScope : KSClassDeclaration {

  val mutableCandidates: Sequence<KSClassDeclaration>
  val logger: KSPLogger
  val typeVariableNames: List<TypeVariableName>
  val target: ClassName
  val mutable: ClassName
  val properties: Sequence<KSPropertyDeclaration>

  val ClassName.parameterized: TypeName

  fun KSType.hasMutableCopy(): Boolean = declaration.closestClassDeclaration() in mutableCandidates
  fun KSPropertyDeclaration.hasMutableCopy(): Boolean = type.resolve().hasMutableCopy()
  fun KSPropertyDeclaration.toAssignment(mutablePostfix: String, source: String? = null): String =
    "$name = ${source ?: ""}$name${mutablePostfix.takeIf { hasMutableCopy() } ?: ""}"

  fun Sequence<KSPropertyDeclaration>.joinAsAssignments(mutablePostfix: String, source: String? = null) =
    joinToString { it.toAssignment(mutablePostfix, source) }

  val KSPropertyDeclaration.typeName: TypeName get() = type.toTypeName(this@TypeCompileScope.typeParameters.toTypeParameterResolver())


  fun toFileScope(file: FileSpec.Builder): FileCompilerScope

}

internal class ClassCompileScope(
  private val classDeclaration: KSClassDeclaration,
  override val mutableCandidates: Sequence<KSClassDeclaration>,
  override val logger: KSPLogger,
) : TypeCompileScope, KSClassDeclaration by classDeclaration {

  override val typeVariableNames: List<TypeVariableName> = typeParameters.map { it.toTypeVariableName() }
  override val target: ClassName = className
  override val mutable: ClassName = target.map { "Mutable$simpleName" }
  override val properties: Sequence<KSPropertyDeclaration> = getAllProperties()

  override val ClassName.parameterized get() = parameterizedWhenNotEmpty(typeVariableNames)

  override fun toFileScope(file: FileSpec.Builder): FileCompilerScope =
    FileCompilerScope(this, file = file)
}

internal class FileCompilerScope(
  parent: TypeCompileScope,
  val file: FileSpec.Builder,
) : TypeCompileScope by parent {
  override fun toFileScope(file: FileSpec.Builder) = this

  fun addFunction(
    name: String,
    receives: TypeName? = null,
    returns: TypeName? = null,
    block: FunSpec.Builder.() -> Unit = {},
  ) = file.addFunction(FunSpec.builder(name).apply {
    receives?.apply { receiver(receives) }
    returns?.apply { returns(returns) }
    addTypeVariables(typeVariableNames)
  }.apply(block).build())

  fun addInlinedFunction(
    name: String,
    receives: TypeName? = null,
    returns: TypeName? = null,
    block: FunSpec.Builder.() -> Unit = {},
  ) = addFunction(name, receives, returns) {
    addModifiers(KModifier.INLINE)
    block()
  }
}
