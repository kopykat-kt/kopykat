package at.kopyk.utils

import at.kopyk.LoggerScope
import at.kopyk.poet.className
import at.kopyk.poet.makeInvariant
import at.kopyk.poet.parameterizedWhenNotEmpty
import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.KSTypeReference
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

internal sealed interface TypeCompileScope : KSDeclaration, LoggerScope {

  val typeVariableNames: List<TypeVariableName>
  val typeParameterResolver: TypeParameterResolver
  val target: ClassName
  val sealedTypes: Sequence<KSClassDeclaration>
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

internal fun TypeParameterResolver.invariant() = object : TypeParameterResolver {
  override val parametersMap: Map<String, TypeVariableName>
    get() = this@invariant.parametersMap.mapValues { (_, v) -> v.makeInvariant() }
  override fun get(index: String): TypeVariableName =
    this@invariant[index].makeInvariant()
}

internal class ClassCompileScope(
  val classDeclaration: KSClassDeclaration,
  private val mutableCandidates: Sequence<KSDeclaration>,
  override val logger: KSPLogger,
) : TypeCompileScope, KSClassDeclaration by classDeclaration {

  override val typeVariableNames: List<TypeVariableName> =
    classDeclaration.typeParameters.map { it.toTypeVariableName() }
  override val typeParameterResolver: TypeParameterResolver
    get() = classDeclaration.typeParameters.toTypeParameterResolver().invariant()

  override val target: ClassName = classDeclaration.className
  val parentTypes: Sequence<KSType> =
    classDeclaration.superTypes.map { it.resolve() }
  override val sealedTypes: Sequence<KSClassDeclaration> = classDeclaration.sealedTypes
  override val properties: Sequence<KSPropertyDeclaration> = classDeclaration.getPrimaryConstructorProperties()

  override val ClassName.parameterized
    get() = parameterizedWhenNotEmpty(typeVariableNames.map { it.makeInvariant() })
  override fun KSType.hasMutableCopy(): Boolean {
    val closestDecl = declaration.closestClassDeclaration()
    return closestDecl != null && closestDecl in mutableCandidates
  }

  override val KSPropertyDeclaration.typeName: TypeName
    get() = type.toTypeName(typeParameterResolver).makeInvariant()

  override fun toFileScope(file: FileSpec.Builder): FileCompilerScope = FileCompilerScope(this, file = file)
}

internal class TypeAliasCompileScope(
  private val aliasDeclaration: KSTypeAlias,
  private val mutableCandidates: Sequence<KSDeclaration>,
  override val logger: KSPLogger,
) : TypeCompileScope, KSTypeAlias by aliasDeclaration {

  init {
    requireNotNull(aliasDeclaration.ultimateDeclaration)
  }

  override val typeVariableNames: List<TypeVariableName> =
    aliasDeclaration.typeParameters.map { it.toTypeVariableName() }
  override val typeParameterResolver: TypeParameterResolver
    get() = aliasDeclaration.typeParameters.toTypeParameterResolver().invariant()

  override val target: ClassName = aliasDeclaration.className
  override val sealedTypes: Sequence<KSClassDeclaration> =
    aliasDeclaration.ultimateDeclaration?.sealedTypes.orEmpty()
  val typeSubstitution: TypeSubstitution = aliasDeclaration.unravelTypeParameters()
  override val properties: Sequence<KSPropertyDeclaration> =
    aliasDeclaration.ultimateDeclaration?.getPrimaryConstructorProperties().orEmpty().map {
      val originalTy = it.type
      object : KSPropertyDeclaration by it {
        override val type: KSTypeReference = originalTy.substitute(typeSubstitution)
      }
    }

  override val ClassName.parameterized
    get() = parameterizedWhenNotEmpty(typeVariableNames.map { it.makeInvariant() })
  override fun KSType.hasMutableCopy(): Boolean {
    val closestDecl = aliasDeclaration.type.resolve().declaration.closestClassDeclaration()
    return closestDecl != null && closestDecl in mutableCandidates
  }

  override val KSPropertyDeclaration.typeName: TypeName
    get() = type.toTypeName(typeParameterResolver).makeInvariant()

  override fun toFileScope(file: FileSpec.Builder): FileCompilerScope = FileCompilerScope(this, file = file)
}

internal class FileCompilerScope(
  val element: TypeCompileScope,
  val file: FileSpec.Builder,
) {

  fun addFunction(
    name: String,
    receives: TypeName?,
    returns: TypeName,
    block: FunSpec.Builder.() -> Unit = {},
  ) {
    file.addFunction(
      FunSpec.builder(name).apply {
        if (receives != null) receiver(receives)
        returns(returns)
        addTypeVariables(element.typeVariableNames.map { it.makeInvariant() })
      }.apply(block).build()
    )
  }

  fun addInlinedFunction(
    name: String,
    receives: TypeName?,
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
