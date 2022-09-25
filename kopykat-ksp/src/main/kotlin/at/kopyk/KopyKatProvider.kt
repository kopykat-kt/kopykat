package at.kopyk

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

public class KopyKatProvider : SymbolProcessorProvider {
  override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
    KopyKatProcessor(
      codegen = environment.codeGenerator,
      logger = environment.logger,
      options = KopyKatOptions.fromKspOptions(environment.options)
    )
}
