package fp.serrano.kopykat

internal fun String.failsWith(check: (String) -> Boolean) {
  failsWith(TransformativeProvider(), check)
}

internal fun String.evals(vararg things: Pair<String, Any?>) {
  evals(TransformativeProvider(), *things)
}
