@file:Suppress("WildcardImport")

package fp.serrano.kopykat

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ParameterSpec
import fp.serrano.kopykat.utils.TypeCompileScope
import fp.serrano.kopykat.utils.addGeneratedMarker
import fp.serrano.kopykat.utils.kotlin.poet.asTransformLambda
import fp.serrano.kopykat.utils.kotlin.poet.buildFile
import fp.serrano.kopykat.utils.ksp.TypeCategory.Known.Data
import fp.serrano.kopykat.utils.ksp.TypeCategory.Known.Sealed
import fp.serrano.kopykat.utils.ksp.TypeCategory.Known.Value
import fp.serrano.kopykat.utils.ksp.category
import fp.serrano.kopykat.utils.name
import fp.serrano.kopykat.utils.sealedTypes

internal val TypeCompileScope.copyMapFunctionKt: FileSpec
  get() = buildFile(fileName = "${target.simpleName}KopyKat") {
    file.addGeneratedMarker()
    addInlinedFunction(name = "copyMap", receives = target.parameterized, returns = target.parameterized) {
      val propertyStatements = properties.map { property ->
        addParameter(
          ParameterSpec.builder(name = property.name, type = property.typeName.asTransformLambda())
            .defaultValue("{ it }").build()
        )
        "${property.name} = ${property.name}(this.${property.name})"
      }
      addReturn(repeatOnSubclasses(propertyStatements.joinToString(), "copy"))
    }
  }

private fun TypeCompileScope.repeatOnSubclasses(
  line: String,
  functionName: String,
): String = when (category) {
  Value -> "$name($line)"
  Data -> "$functionName($line)"
  Sealed -> sealedTypes.joinWithWhen { "is ${it.name} -> $functionName($line)" }
  else -> error("Unknown category for ${target.simpleName}")
}

internal fun <A> Sequence<A>.joinWithWhen(subject: String = "this", transform: (A) -> String = { it.toString() }) =
  joinToString(prefix = "when ($subject) {\n", separator = "\n", postfix = "\n}", transform = transform)
