@file:Suppress("WildcardImport")
package fp.serrano.kopykat

import com.google.devtools.ksp.processing.*

public class KopyKatProvider : SymbolProcessorProvider {
  override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
    KopyKatProcessor(
      codegen = environment.codeGenerator,
      logger = environment.logger,
      options = KopyKatOptions.fromKspOptions(environment.options)
    )
}
