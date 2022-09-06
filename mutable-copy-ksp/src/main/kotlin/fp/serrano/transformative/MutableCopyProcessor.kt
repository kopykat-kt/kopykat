package fp.serrano.transformative

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.*

class MutableCopyProcessor(private val codegen: CodeGenerator, private val logger: KSPLogger) : SymbolProcessor {
  companion object {
    const val ANNOTATION_NAME = "fp.serrano.MutableCopy"
  }

  override fun process(resolver: Resolver): List<KSAnnotated> {
    resolver
      .getSymbolsWithAnnotation(ANNOTATION_NAME)
      .filterIsInstance<KSClassDeclaration>()
      .forEach(::processClass)
    return emptyList()
  }

  private fun processClass(klass: KSClassDeclaration) {
    if (Modifier.DATA !in klass.modifiers || klass.primaryConstructor == null) {
      logger.error(klass.notDataClassErrorMessage, klass)
      return
    }

    // TODO: check for mutability and fail?

    val packageName = klass.packageName.asString()
    val targetTypeName = klass.simpleName.asString()
    val mutableTypeName = "Mutable$targetTypeName"
    val properties = klass.getAllProperties()
    val typeParameters = klass.typeParameters
    val typeParamResolver = typeParameters.toTypeParameterResolver()
    val typeVariables = typeParameters.map { it.toTypeVariableName() }
    val targetClassName = ClassName(packageName, targetTypeName)
    val mutableClassName = ClassName(packageName, mutableTypeName)
    val supertypes = klass.superTypes.map { it.toTypeName() }

    buildFile(
      packageName = packageName,
      fileName = mutableTypeName,
    ) {
      addClass(mutableClassName) {
        supertypes.forEach(::superclass)
        addTypeVariables(typeVariables)
        primaryConstructor {
          addParameters(properties.map { property -> property.toParameterSpec(typeParamResolver) }.asIterable())
        }
        addProperties(properties.map { property ->
          property.asPropertySpec(typeParamResolver) {
            mutable(true)
            initializer(property.simpleName.asString())
          }
        }.asIterable())
      }
      addFunction("copy") {
        receiver(targetClassName.parameterizedByIfNotEmpty(typeVariables))
        returns(targetClassName.parameterizedByIfNotEmpty(typeVariables))
        addTypeVariables(typeVariables)
        addParameter(
          name = "block",
          type = LambdaTypeName.get(
            receiver = mutableClassName.parameterizedByIfNotEmpty(typeVariables),
            returnType = UNIT,
          )
        )
        val typeParams =
          typeParameters
            .takeUnless { it.isEmpty() }
            ?.joinToString(prefix = "<", postfix = ">") { it.name.asString() }
            .orEmpty()
        addCode(
          """
          | val mutable = $mutableTypeName$typeParams(
          |   ${properties.map { "${it.name} = ${it.name}" }.joinToString()}
          | ).apply(block)
          | return $targetTypeName$typeParams(
          |   ${properties.map { "${it.name} = mutable.${it.name}" }.joinToString()}
          | )
          """.trimMargin()
        )
      }
    }.writeTo(codeGenerator = codegen, aggregating = false)
  }
}

private val KSDeclaration.name get() = simpleName.asString()
