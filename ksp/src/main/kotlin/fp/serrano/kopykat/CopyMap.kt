@file:Suppress("WildcardImport")

package fp.serrano.kopykat

import com.squareup.kotlinpoet.FileSpec
import fp.serrano.kopykat.utils.TypeCategory.Known.Data
import fp.serrano.kopykat.utils.TypeCategory.Known.Sealed
import fp.serrano.kopykat.utils.TypeCategory.Known.Value
import fp.serrano.kopykat.utils.TypeCompileScope
import fp.serrano.kopykat.utils.addGeneratedMarker
import fp.serrano.kopykat.utils.lang.joinWithWhen
import fp.serrano.kopykat.utils.lang.mapRun
import fp.serrano.kopykat.utils.lang.onEachRun
import fp.serrano.kopykat.utils.name
import fp.serrano.kopykat.utils.sealedTypes
import fp.serrano.kopykat.utils.typeCategory

internal val TypeCompileScope.copyMapFunctionKt: FileSpec
  get() = buildFile(fileName = "${target.simpleName}KopyKat") {
    addGeneratedMarker()
    addInlinedFunction(name = "copyMap", receives = target.parameterized, returns = target.parameterized) {
      properties
        .onEachRun { addParameter(name = name, type = typeName.asTransformLambda(), defaultValue = "{ it }") }
        .mapRun { "$name = $name(this.$name)" }
        .run { addReturn(repeatOnSubclasses(joinToString(), "copy")) }
    }
  }

private fun TypeCompileScope.repeatOnSubclasses(
  line: String,
  functionName: String,
): String = when (typeCategory) {
  Value -> "$name($line)"
  Data -> "$functionName($line)"
  Sealed -> sealedTypes.joinWithWhen { "is ${it.name} -> $functionName($line)" }
  else -> error("Unknown type category for ${target.simpleName}")
}
