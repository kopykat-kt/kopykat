package at.kopyk.utils

import at.kopyk.poet.className
import at.kopyk.poet.makeInvariant
import at.kopyk.poet.parameterizedWhenNotEmpty
import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.SET
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeVariableName

internal data class MutationInfo<out T : TypeName>(
  val className: T,
  val toMutable: (String) -> String,
  val freeze: (String) -> String
)

internal sealed interface TypeCompileScope : KSClassDeclaration {

  val logger: KSPLogger
  val typeVariableNames: List<TypeVariableName>
  val typeParameterResolver: TypeParameterResolver
  val target: ClassName
  val properties: Sequence<KSPropertyDeclaration>
  val mutationInfo: Sequence<Pair<KSPropertyDeclaration, MutationInfo<TypeName>>>
    get() = properties.map { it to mutationInfo(it.type.resolve()) }

  val ClassName.parameterized: TypeName
  val KSPropertyDeclaration.typeName: TypeName
  fun KSType.hasMutableCopy(): Boolean

  fun KSPropertyDeclaration.toAssignment(wrapper: (String) -> String, source: String? = null): String =
    "$baseName = ${wrapper("${source ?: ""}$baseName")}"
  fun Sequence<Pair<KSPropertyDeclaration, MutationInfo<TypeName>>>.joinAsAssignmentsWithMutation(
    wrapper: MutationInfo<TypeName>.(String) -> String,
  ) = joinToString { (prop, mut) -> prop.toAssignment({ wrapper(mut, it) }) }

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
  override val typeParameterResolver: TypeParameterResolver
    get() {
      val original = typeParameters.toTypeParameterResolver()
      return object : TypeParameterResolver {
        override val parametersMap: Map<String, TypeVariableName>
          get() = original.parametersMap.mapValues { (_, v) -> v.makeInvariant() }
        override fun get(index: String): TypeVariableName =
          original.get(index).makeInvariant()
      }
    }
  override val target: ClassName = className
  override val properties: Sequence<KSPropertyDeclaration> = getPrimaryConstructorProperties()

  override val ClassName.parameterized
    get() = parameterizedWhenNotEmpty(typeVariableNames.map { it.makeInvariant() })
  override fun KSType.hasMutableCopy(): Boolean = declaration.closestClassDeclaration() in mutableCandidates

  override val KSPropertyDeclaration.typeName: TypeName
    get() = type.toTypeName(classDeclaration.typeParameters.toTypeParameterResolver()).makeInvariant()

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
    file.addFunction(
      FunSpec.builder(name).apply {
        receiver(receives)
        returns(returns)
        addTypeVariables(typeVariableNames.map { it.makeInvariant() })
      }.apply(block).build()
    )
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

internal fun TypeCompileScope.mutationInfo(ty: KSType): MutationInfo<TypeName> =
  when (ty.declaration) {
    is KSClassDeclaration -> {
      val className = ty.toClassName()
      val intermediate: MutationInfo<ClassName> = when {
        className == LIST ->
          MutationInfo(
            ClassName(className.packageName, "MutableList"),
            { "$it.toMutableList()" },
            { it }
          )
        className == MAP ->
          MutationInfo(
            ClassName(className.packageName, "MutableMap"),
            { "$it.toMutableMap()" },
            { it }
          )
        className == SET ->
          MutationInfo(
            ClassName(className.packageName, "MutableSet"),
            { "$it.toMutableSet()" },
            { it }
          )
        ty.hasMutableCopy() ->
          MutationInfo(
            className.mutable,
            { "$it.toMutable()" },
            { "$it.freeze()" }
          )
        else ->
          MutationInfo(className, { it }, { it })
      }
      MutationInfo(
        className = intermediate.className.parameterizedWhenNotEmpty(
          ty.arguments.map { it.toTypeName(typeParameterResolver) }
        ),
        toMutable = intermediate.toMutable,
        freeze = intermediate.freeze
      )
    }
    else ->
      MutationInfo(ty.toTypeName(typeParameterResolver), { it }, { it })
  }
