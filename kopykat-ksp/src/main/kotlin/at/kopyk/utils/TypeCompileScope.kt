package at.kopyk.utils

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
import at.kopyk.poet.className
import at.kopyk.poet.parameterizedWhenNotEmpty

internal sealed interface TypeCompileScope : KSClassDeclaration {

  val logger: KSPLogger
  val typeVariableNames: List<TypeVariableName>
  val target: ClassName
  val properties: Sequence<KSPropertyDeclaration>

  val ClassName.parameterized: TypeName
  val KSPropertyDeclaration.typeName: TypeName
  fun KSType.hasMutableCopy(): Boolean

  fun KSPropertyDeclaration.hasMutableCopy(): Boolean = type.resolve().hasMutableCopy()
  fun KSPropertyDeclaration.toAssignment(mutablePostfix: String, source: String? = null): String =
    "$baseName = ${source ?: ""}$baseName${mutablePostfix.takeIf { hasMutableCopy() } ?: ""}"

  fun Sequence<KSPropertyDeclaration>.joinAsAssignments(mutablePostfix: String, source: String? = null) =
    joinToString { it.toAssignment(mutablePostfix, source) }

  fun buildFile(fileName: String, block: FileCompilerScope.() -> Unit): FileSpec =
    FileSpec.builder(packageName.asString(), fileName).also { toFileScope(it).block() }.build()

  fun toFileScope(file: FileSpec.Builder): FileCompilerScope

}

internal class ClassCompileScope(
  private val classDeclaration: KSClassDeclaration,
  private val mutableCandidates: Sequence<KSClassDeclaration>,
  override val logger: KSPLogger,
) : TypeCompileScope, KSClassDeclaration by classDeclaration {

  override val typeVariableNames: List<TypeVariableName> = typeParameters.map { it.toTypeVariableName() }
  override val target: ClassName = className
  override val properties: Sequence<KSPropertyDeclaration> = getPrimaryConstructorProperties()

  override val ClassName.parameterized get() = parameterizedWhenNotEmpty(typeVariableNames)
  override fun KSType.hasMutableCopy(): Boolean = declaration.closestClassDeclaration() in mutableCandidates

  override val KSPropertyDeclaration.typeName: TypeName
    get() = type.toTypeName(classDeclaration.typeParameters.toTypeParameterResolver())

  override fun toFileScope(file: FileSpec.Builder): FileCompilerScope = FileCompilerScope(this, file = file)
}

internal class FileCompilerScope(
  parent: TypeCompileScope,
  val file: FileSpec.Builder,
) : TypeCompileScope by parent {

  override fun toFileScope(file: FileSpec.Builder) = this

  fun addFunction(
    name: String,
    receives: TypeName,
    returns: TypeName,
    block: FunSpec.Builder.() -> Unit = {},
  ) {
    file.addFunction(FunSpec.builder(name).apply {
      receiver(receives)
      returns(returns)
      addTypeVariables(typeVariableNames)
    }.apply(block).build())
  }

  fun addInlinedFunction(
    name: String,
    receives: TypeName,
    returns: TypeName,
    block: FunSpec.Builder.() -> Unit = {},
  ) {
    addFunction(name, receives, returns) {
      addModifiers(KModifier.INLINE)
      block()
    }
  }
}
