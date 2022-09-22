package fp.serrano.kopykat.utils.kotlin.poet

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import fp.serrano.kopykat.utils.FileCompilerScope
import fp.serrano.kopykat.utils.TypeCompileScope

internal fun TypeCompileScope.className(first: String, vararg rest: String) =
  ClassName(packageName = packageName.asString(), listOf(first) + rest)

internal fun TypeCompileScope.buildFile(fileName: String, block: FileCompilerScope.() -> Unit): FileSpec =
  FileSpec.builder(packageName.asString(), fileName).also { toFileScope(it).block() }.build()
