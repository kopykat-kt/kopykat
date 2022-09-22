package fp.serrano.kopykat

import com.google.devtools.ksp.symbol.KSClassDeclaration
import fp.serrano.kopykat.utils.isDataClass
import fp.serrano.kopykat.utils.isValueClass
import fp.serrano.kopykat.utils.mapSealedSubclasses
import fp.serrano.kopykat.utils.name

internal fun KSClassDeclaration.repeatOnSubclasses(
  line: String,
  functionName: String,
): String = when {
  isValueClass() -> "${name}($line)"
  isDataClass() -> "$functionName(${line})"
  else -> mapSealedSubclasses { "is ${it.name} -> $functionName($line)" }.joinWithWhen()
}

internal fun Sequence<String>.joinWithWhen(subject: String = "this") =
  joinToString(prefix = "when ($subject) {\n", separator = "\n", postfix = "\n}")