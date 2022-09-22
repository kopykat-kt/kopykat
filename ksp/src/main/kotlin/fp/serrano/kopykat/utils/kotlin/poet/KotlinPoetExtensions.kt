package fp.serrano.kopykat.utils.kotlin.poet

import com.google.devtools.ksp.symbol.KSDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.TypeName
import fp.serrano.kopykat.utils.FileCompilerScope
import fp.serrano.kopykat.utils.TypeCompileScope
import fp.serrano.kopykat.utils.name

internal fun TypeCompileScope.className(first: String, vararg rest: String) =
  ClassName(packageName = packageName.asString(), listOf(first) + rest)

internal val KSDeclaration.className: ClassName get() = ClassName(packageName = packageName.asString(), name)

internal fun ClassName.map(name: (String) -> String) =
  ClassName(packageName, simpleNames.run { dropLast(1) + name(last()) })

internal fun TypeCompileScope.buildFile(fileName: String, block: FileCompilerScope.() -> Unit): FileSpec =
  FileSpec.builder(packageName.asString(), fileName).also { toFileScope(it).block() }.build()

internal fun TypeName.asTransformLambda() = LambdaTypeName.get(parameters = arrayOf(this), returnType = this)

