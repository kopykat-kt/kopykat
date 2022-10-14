package at.kopyk

import at.kopyk.utils.TypeCategory.Known.Sealed
import at.kopyk.utils.isConstructable
import at.kopyk.utils.lang.filterIsInstance
import at.kopyk.utils.lang.forEachRun
import at.kopyk.utils.lang.mapRun
import at.kopyk.utils.lang.onEachRun
import at.kopyk.utils.typeCategory
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration

internal class KopyKatProcessor(
  private val codegen: CodeGenerator,
  private val logger: KSPLogger,
  private val options: KopyKatOptions,
) : SymbolProcessor {

  override fun process(resolver: Resolver): List<KSAnnotated> {
    resolver.getAllFiles().inScope(codegen, logger, options) {
      // add different copies to data, value classes, and type aliases
      (classes.mapRun { classScope } + typealiases.mapRun { typealiasScope })
        .filterNot { it.typeCategory is Sealed && !options.hierarchyCopy }
        .onEachRun { logger.logging("Processing $simpleName") }
        .forEachRun {
          if (options.copyMap) copyMapFunctionKt.write()
          if (options.mutableCopy) mutableCopyKt.write()
        }

      // add copy from parent to all classes
      declarations
        .filterIsInstance<KSClassDeclaration> { !isAbstract() && isConstructable() }
        .forEachRun { if (options.superCopy) classScope.copyFromParentKt.write() }
    }
    return emptyList()
  }

}
