package at.kopyk

import at.kopyk.compiletesting.compilesWith
import at.kopyk.compiletesting.evals
import at.kopyk.compiletesting.failsWith

internal fun String.failsWith(providerArgs: Map<String, String> = emptyMap(), check: (String) -> Boolean) {
  failsWith(KopyKatProvider(), providerArgs, check)
}

internal fun String.compilesWith(providerArgs: Map<String, String> = emptyMap(), check: (String) -> Boolean) {
  compilesWith(KopyKatProvider(), providerArgs, check)
}

internal fun String.evalsWithArgs(providerArgs: Map<String, String> = emptyMap(), vararg things: Pair<String, Any?>) {
  evals(KopyKatProvider(), providerArgs, *things)
}

internal fun String.evals(vararg things: Pair<String, Any?>) {
  evals(KopyKatProvider(), emptyMap(), *things)
}
