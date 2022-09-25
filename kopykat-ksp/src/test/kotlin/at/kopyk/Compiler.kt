package at.kopyk

import at.kopyk.compiletesting.evals
import at.kopyk.compiletesting.failsWith

internal fun String.failsWith(check: (String) -> Boolean) {
  failsWith(KopyKatProvider(), check)
}

internal fun String.evals(vararg things: Pair<String, Any?>) {
  evals(KopyKatProvider(), *things)
}
