package at.kopyk

import at.kopyk.poet.append
import com.squareup.kotlinpoet.FileSpec
import at.kopyk.poet.addParameter
import at.kopyk.poet.addReturn
import at.kopyk.poet.asTransformLambda
import at.kopyk.utils.TypeCategory.Known.Data
import at.kopyk.utils.TypeCategory.Known.Sealed
import at.kopyk.utils.TypeCategory.Known.Value
import at.kopyk.utils.TypeCompileScope
import at.kopyk.utils.addGeneratedMarker
import at.kopyk.utils.baseName
import at.kopyk.utils.fullName
import at.kopyk.utils.lang.joinWithWhen
import at.kopyk.utils.lang.mapRun
import at.kopyk.utils.lang.onEachRun
import at.kopyk.utils.sealedTypes
import at.kopyk.utils.typeCategory

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
  Value -> "$fullName($line)"
  Data -> "$functionName($line)"
  Sealed -> sealedTypes.joinWithWhen { "is ${it.fullName} -> $functionName($line)" }
  else -> error("Unknown type category for ${target.canonicalName}")
}
