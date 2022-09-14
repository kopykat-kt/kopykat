package fp.serrano.kopykat

internal fun String.failsWith(check: (String) -> Boolean) {
  failsWith(KopyKatProvider(), check)
}

internal fun String.evals(vararg things: Pair<String, Any?>) {
  evals(KopyKatProvider(), *things)
}
