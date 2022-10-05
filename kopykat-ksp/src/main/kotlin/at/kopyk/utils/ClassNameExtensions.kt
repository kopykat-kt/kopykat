package at.kopyk.utils

import at.kopyk.poet.flattenWithSuffix
import com.squareup.kotlinpoet.ClassName

internal val ClassName.mutable: ClassName get() = flattenWithSuffix("Mutable")
internal val ClassName.dslMarker: ClassName get() = flattenWithSuffix("DslMarker")
