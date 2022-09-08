package fp.serrano.transformative

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.*

private const val ANNOTATION_NAME = "fp.serrano.Transformative"

internal class TransformativeProcessor(private val codegen: CodeGenerator, private val logger: KSPLogger) :
  SymbolProcessor {

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

    val packageName = klass.packageName.asString()
    val targetTypeName = klass.simpleName.asString()
    val typeParameters = klass.typeParameters
    val typeVariables = typeParameters.map { it.toTypeVariableName() }

    transformFunction(
      packageName = packageName,
      klass = klass,
      targetClassName = ClassName(packageName, targetTypeName).parameterizedWhenNotEmpty(typeVariables),
      typeVariables = typeVariables,
      typeParamResolver = typeParameters.toTypeParameterResolver()
    ).writeTo(codeGenerator = codegen, aggregating = false)

    val mutableTypeName = "Mutable$targetTypeName"

    mutableCopy(
      packageName = packageName,
      mutableTypeName = mutableTypeName,
      mutableClassName = ClassName(packageName, mutableTypeName),
      typeVariables = typeVariables,
      properties = klass.getAllProperties(),
      typeParamResolver = typeParameters.toTypeParameterResolver(),
      targetClassName = ClassName(packageName, targetTypeName).parameterizedWhenNotEmpty(typeVariables)
    ).writeTo(codeGenerator = codegen, aggregating = false)
  }
}
