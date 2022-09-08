package fp.serrano.transformative

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
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

    klass.toTransformFunctionKt().writeTo(codeGenerator = codegen, aggregating = false)

    klass.toMutableCopyKt().writeTo(codeGenerator = codegen, aggregating = false)
  }
}
