package at.kopyk.utils.lang

internal inline fun <reified T> Any?.takeIfInstanceOf(): T? =
  this as? T
