package at.kopyk.utils

import com.squareup.kotlinpoet.ClassName
import at.kopyk.poet.flattenWithSuffix

internal val ClassName.mutable: ClassName get() = flattenWithSuffix("Mutable")
internal val ClassName.dslMarker: ClassName get() = flattenWithSuffix("DslMarker")
