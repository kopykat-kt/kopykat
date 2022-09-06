package fp.serrano.transformative

import com.google.devtools.ksp.symbol.KSDeclaration

val KSDeclaration.notDataClassErrorMessage
  get() =
    """
      |${(qualifiedName ?: simpleName).asString()} cannot be annotated with @MutableCopy
      | ^
      |Only data classes can be annotated with @MutableCopy""".trimMargin()