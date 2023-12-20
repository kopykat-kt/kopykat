@file:OptIn(ExperimentalCompilerApi::class)

package at.kopyk.compiletesting

import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.tschuchort.compiletesting.CompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode.OK
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspArgs
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.io.File
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Paths
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi

private const val SOURCE_FILENAME = "Source.kt"

public fun String.failsWith(
  provider: SymbolProcessorProvider,
  providerArgs: Map<String, String> = emptyMap(),
  check: (String) -> Boolean
) {
  val compilationResult = compile(this, provider, providerArgs)
  compilationResult.exitCode shouldNotBe OK
  check(compilationResult.messages) shouldBe true
}

public fun String.compilesWith(
  provider: SymbolProcessorProvider,
  providerArgs: Map<String, String> = emptyMap(),
  check: (String) -> Boolean
) {
  val compilationResult = compile(this, provider, providerArgs)

  compilationResult.exitCode shouldBe OK
  check(compilationResult.messages) shouldBe true
}

public fun String.evals(
  provider: SymbolProcessorProvider,
  providerArgs: Map<String, String> = emptyMap(),
  vararg things: Pair<String, Any?>
) {
  val compilationResult = compile(this, provider, providerArgs)
  compilationResult.exitCode shouldBe OK
  val classesDirectory = compilationResult.outputDirectory
  things.forEach { (variable, output) ->
    eval(variable, classesDirectory) shouldBe output
  }
}

// UTILITY FUNCTIONS COPIED FROM META-TEST
// =======================================

internal data class FullCompilationResult(
  val mainResult: CompilationResult,
  val additionalMessages: String?
) {
  val exitCode = mainResult.exitCode
  val outputDirectory = mainResult.outputDirectory
  val messages = when (additionalMessages) {
    null -> mainResult.messages
    else -> listOf(additionalMessages, mainResult.messages).joinToString(separator = "\n")
  }
}

private fun CompilationResult.pass1Result() =
  FullCompilationResult(this, null)

private fun CompilationResult.pass2Result(additionalMessages: String) =
  FullCompilationResult(this, additionalMessages)

internal fun compile(
  text: String,
  provider: SymbolProcessorProvider,
  providerArgs: Map<String, String> = emptyMap()
): FullCompilationResult {
  val compilation = buildCompilation(text, provider, providerArgs)
  // fix problems with double compilation and KSP
  // as stated in https://github.com/tschuchortdev/kotlin-compile-testing/issues/72
  val pass1 = compilation.compile()
  // if the first pass was unsuccessful, return it
  if (pass1.exitCode != OK) return pass1.pass1Result()
  // return the results of second pass
  return buildCompilation(text, provider)
    .apply {
      sources = compilation.sources + compilation.kspGeneratedSourceFiles
      symbolProcessorProviders = emptyList()
    }
    .compile()
    .pass2Result(pass1.messages)
}

private fun buildCompilation(
  text: String,
  provider: SymbolProcessorProvider,
  providerArgs: Map<String, String> = emptyMap()
) = KotlinCompilation().apply {
  symbolProcessorProviders = listOf(provider)
  kspArgs.putAll(providerArgs)
  sources = listOf(SourceFile.kotlin(SOURCE_FILENAME, text.trimMargin()))
  inheritClassPath = true
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
