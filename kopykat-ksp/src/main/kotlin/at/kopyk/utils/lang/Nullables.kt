package at.kopyk.utils.lang

internal inline fun <T> T?.orElse(block: () -> T): T = this ?: block()
