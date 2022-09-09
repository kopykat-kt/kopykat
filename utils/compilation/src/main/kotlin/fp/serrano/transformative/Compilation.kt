package fp.serrano.transformative

import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.assertj.core.api.Assertions
import java.io.File
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Paths

private const val SOURCE_FILENAME = "Source.kt"

public fun String.failsWith(provider: SymbolProcessorProvider, check: (String) -> Boolean) {
  val compilationResult = compile(this, provider)
  Assertions.assertThat(compilationResult.exitCode).isNotEqualTo(KotlinCompilation.ExitCode.OK)
  Assertions.assertThat(check(compilationResult.messages)).isTrue
}

public fun String.evals(provider: SymbolProcessorProvider, vararg things: Pair<String, Any?>) {
  val compilationResult = compile(this, provider)
  Assertions.assertThat(compilationResult.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
  val classesDirectory = compilationResult.outputDirectory
  things.forEach { (variable, output) ->
    Assertions.assertThat(eval(variable, classesDirectory)).isEqualTo(output)
  }
}

// UTILITY FUNCTIONS COPIED FROM META-TEST
// =======================================

internal fun compile(text: String, provider: SymbolProcessorProvider): KotlinCompilation.Result {
  val compilation = buildCompilation(text, provider)
  // fix problems with double compilation and KSP
  // as stated in https://github.com/tschuchortdev/kotlin-compile-testing/issues/72
  val pass1 = compilation.compile()
  // if the first pass was unsuccessful, return it
  if (pass1.exitCode != KotlinCompilation.ExitCode.OK) return pass1
  // return the results of second pass
  return buildCompilation(text, provider)
    .apply {
      sources = compilation.sources + compilation.kspGeneratedSourceFiles
      symbolProcessorProviders = emptyList()
    }
    .compile()
}

private fun buildCompilation(
  text: String,
  provider: SymbolProcessorProvider,
) = KotlinCompilation().apply {
  symbolProcessorProviders = listOf(provider)
  sources = listOf(SourceFile.kotlin(SOURCE_FILENAME, text.trimMargin()))
}


private val KotlinCompilation.kspGeneratedSourceFiles: List<SourceFile>
  get() =
    kspSourcesDir
      .resolve("kotlin")
      .walk()
      .filter { it.isFile }
      .map { SourceFile.fromPath(it.absoluteFile) }
      .toList()

private fun eval(expression: String, classesDirectory: File): Any? {
  val classLoader = URLClassLoader(arrayOf(classesDirectory.toURI().toURL()))
  val fullClassName = getFullClassName(classesDirectory)
  val field = classLoader.loadClass(fullClassName).getDeclaredField(expression)
  field.isAccessible = true
  return field.get(Object())
}

private fun getFullClassName(classesDirectory: File): String =
  Files.walk(Paths.get(classesDirectory.toURI()))
    .filter { it.toFile().name == "SourceKt.class" }
    .toArray()[0]
    .toString()
    .removePrefix(classesDirectory.absolutePath + File.separator)
    .removeSuffix(".class")
    .replace(File.separator, ".")
