package at.kopyk

import at.kopyk.poet.writeTo
import at.kopyk.utils.ClassCompileScope
import at.kopyk.utils.TypeAliasCompileScope
import at.kopyk.utils.TypeCategory.Known
import at.kopyk.utils.TypeCategory.Known.Data
import at.kopyk.utils.TypeCategory.Known.Sealed
import at.kopyk.utils.TypeCategory.Known.Value
import at.kopyk.utils.TypeCompileScope
import at.kopyk.utils.allNestedDeclarations
import at.kopyk.utils.hasGeneratedMarker
import at.kopyk.utils.lang.forEachRun
import at.kopyk.utils.onKnownCategory
import at.kopyk.utils.typeCategory
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSTypeAlias
import org.apache.commons.io.FilenameUtils.wildcardMatch

internal class KopyKatProcessor(
  private val codegen: CodeGenerator,
  private val logger: KSPLogger,
  private val options: KopyKatOptions
) : SymbolProcessor {

  @OptIn(KspExperimental::class)
  override fun process(resolver: Resolver): List<KSAnnotated> {
    resolver.getAllFiles().let { files ->
      if (files.none { it.hasGeneratedMarker() }) {
        val declarations = files.flatMap { it.allNestedDeclarations() }

        val classes = declarations
          .filterIsInstance<KSClassDeclaration>()
          .onEach { it.checkKnown() }
          .onEach { it.checkRedundantAnnotation() }
          .filter { it.shouldGenerate() && it.typeCategory is Known }

        classes
          .let { targets -> targets.map { ClassCompileScope(it, classes, logger) } }
          .forEachRun { process() }

        declarations
          .filterIsInstance<KSTypeAlias>()
          .onEach { it.checkKnown() }
          .filter { it.isAnnotationPresent(CopyExtensions::class) && it.typeCategory is Known }
          .let { targets -> targets.map { TypeAliasCompileScope(it, classes, logger) } }
          .forEachRun { process() }
      }
    }
    return emptyList()
  }

  private fun TypeCompileScope.process() {
    logger.logging("Processing $simpleName")
    fun mapAndMutable() {
      if (options.copyMap) copyMapFunctionKt.writeTo(codegen)
      if (options.mutableCopy) mutableCopyKt.writeTo(codegen)
    }
    onKnownCategory { category ->
      when (category) {
        Data, Value -> {
          mapAndMutable()
          if (options.superCopy && this is ClassCompileScope) copyFromParentKt.writeTo(codegen)
        }
        Sealed -> if (options.hierarchyCopy) mapAndMutable()
      }
    }
  }

  @OptIn(KspExperimental::class)
  private fun KSDeclaration.shouldGenerate(): Boolean = when (options.generate) {
    is KopyKatGenerate.Error ->
      false
    is KopyKatGenerate.All ->
      true
    is KopyKatGenerate.Annotated ->
      isAnnotationPresent(CopyExtensions::class)
    is KopyKatGenerate.Packages -> {
      val pkg = packageName.asString()
      options.generate.patterns.any { pattern ->
        wildcardMatch(pkg, pattern)
      }
    }
  }

  @OptIn(KspExperimental::class)
  private fun KSDeclaration.checkKnown() {
    if (isAnnotationPresent(CopyExtensions::class) && typeCategory !is Known) {
      logger.error(
        """
        '@CopyExtensions' may only be used in data or value classes,
        sealed hierarchies of those, or type aliases of those.
        """.trimIndent(),
        this
      )
    }
  }

  @OptIn(KspExperimental::class)
  private fun KSDeclaration.checkRedundantAnnotation() {
    if (isAnnotationPresent(CopyExtensions::class) && options.generate is KopyKatGenerate.NotAnnotated) {
      logger.warn(
        """
        Unused '@CopyExtensions' annotation, the plug-in is configured to process all classes.
        Add 'arg("annotatedOnly", "true")' to your KSP configuration to change this option.
        More info at https://kopyk.at/#enable-only-for-selected-classes.
        """.trimIndent(),
        this
      )
    }
  }
}
