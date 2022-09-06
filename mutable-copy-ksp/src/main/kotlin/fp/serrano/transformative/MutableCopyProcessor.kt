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
    val targetClassName = ClassName(packageName, targetTypeName).parameterizedWhenNotEmpty(typeVariables)
    val mutableClassName = ClassName(packageName, mutableTypeName)
    buildFile(packageName = packageName, fileName = mutableTypeName) {
      addClass(mutableClassName) {
        addTypeVariables(typeVariables)
        primaryConstructor {
          properties.forEach { property ->
            addParameter(property.asParameterSpec(typeParamResolver))
            addProperty(property.asPropertySpec(typeParamResolver) {
              mutable(true).initializer(property.simpleName.asString())
            })
          }
        }
      }
      addFunction("copy") {
        receiver(targetClassName)
        returns(targetClassName)
        addTypeVariables(typeVariables)
        val mutable = mutableClassName.parameterizedWhenNotEmpty(typeVariables)
        addParameter(name = "block", type = LambdaTypeName.get(receiver = mutable, returnType = UNIT))
        addCode(
          """
          | val mutable = $mutable(${properties.joinToString { "${it.name} = ${it.name}" } }).apply(block)
          | return $targetClassName(${properties.joinToString { "${it.name} = mutable.${it.name}" } })
          """.trimMargin()
        )
      }
    }.writeTo(codeGenerator = codegen, aggregating = false)
  }
}

private val KSDeclaration.name get() = simpleName.asString()
