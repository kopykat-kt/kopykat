@file:Suppress("WildcardImport")

package fp.serrano.kopykat

import com.squareup.kotlinpoet.FileSpec
import fp.serrano.kopykat.utils.*
import fp.serrano.kopykat.utils.TypeCategory.Known.Data
import fp.serrano.kopykat.utils.TypeCategory.Known.Sealed
import fp.serrano.kopykat.utils.TypeCategory.Known.Value
import fp.serrano.kopykat.utils.TypeCompileScope
import fp.serrano.kopykat.utils.addGeneratedMarker
import fp.serrano.kopykat.utils.lang.joinWithWhen
import fp.serrano.kopykat.utils.lang.mapRun
import fp.serrano.kopykat.utils.lang.onEachRun
import fp.serrano.kopykat.utils.sealedTypes
import fp.serrano.kopykat.utils.typeCategory

internal val TypeCompileScope.copyMapFunctionKt: FileSpec
  get() = buildFile(fileName = target.append("CopyMap").reflectionName()) {
    addGeneratedMarker()
    addInlinedFunction(name = "copyMap", receives = target.parameterized, returns = target.parameterized) {
      properties
        .onEachRun { addParameter(name = baseName, type = typeName.asTransformLambda(), defaultValue = "{ it }") }
        .mapRun { "$baseName = $baseName(this.$baseName)" }
        .run { addReturn(repeatOnSubclasses(joinToString(), "copy")) }
    }
  }

private fun TypeCompileScope.repeatOnSubclasses(
  line: String,
  functionName: String,
): String = when (typeCategory) {
  Value -> "$qfName($line)"
  Data -> "$functionName($line)"
  Sealed -> sealedTypes.joinWithWhen { "is ${it.qfName} -> $functionName($line)" }
  else -> error("Unknown type category for ${target.canonicalName}")
}
