package at.kopyk

import at.kopyk.poet.writeTo
import at.kopyk.utils.ClassCompileScope
import at.kopyk.utils.TypeAliasCompileScope
import at.kopyk.utils.TypeCategory.Known
import at.kopyk.utils.allNestedDeclarations
import at.kopyk.utils.hasAnnotation
import at.kopyk.utils.hasGeneratedMarker
import at.kopyk.utils.lang.filterIsInstance
import at.kopyk.utils.typeCategory
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.squareup.kotlinpoet.FileSpec
import org.apache.commons.io.FilenameUtils

internal fun ProcessorScope.processFiles(
  resolver: Resolver,
  block: FileCompileScope.() -> Unit,
) {
  val files = resolver.getAllFiles()
  if (files.none(KSFile::hasGeneratedMarker)) {
    block(FileCompileScope(files, this))
  }
}

internal class FileCompileScope(
  files: Sequence<KSFile>,
  scope: ProcessorScope,
) : LoggerScope by scope, OptionsScope by scope {

  private val codegen: CodeGenerator = scope.codegen

  val declarations = files
    .flatMap { it.allNestedDeclarations() }
    .onEach { it.isKnownWithCopyExtension() }
    .onEach { it.checkRedundantAnnotation() }
    .filter { it.typeCategory is Known }

  val typeAliases = declarations
    .filterIsInstance<KSTypeAlias> { isKnownWithCopyExtension() }

  val classes = declarations
    .filterIsInstance<KSClassDeclaration>()
    .filter { it.shouldGenerate() }

  val KSClassDeclaration.classScope: ClassCompileScope
    get() = ClassCompileScope(this, classes, logger)

  val KSTypeAlias.typealiasScope: TypeAliasCompileScope
    get() = TypeAliasCompileScope(this, classes, logger)

  private fun KSDeclaration.isKnownWithCopyExtension() =
    hasAnnotation<CopyExtensions>()
      .also { isCopyExtension ->
        if (isCopyExtension && (typeCategory !is Known || typeCategory is Known.Class)) {
          logger.error(
            """
            '@CopyExtensions' may only be used in data or value classes,
            sealed hierarchies of those, or type aliases of those.
            """.trimIndent(),
            this
          )
        }
      }

  fun FileSpec.write() {
    writeTo(codegen)
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
        FilenameUtils.wildcardMatch(pkg, pattern)
      }
    }
  }
}
