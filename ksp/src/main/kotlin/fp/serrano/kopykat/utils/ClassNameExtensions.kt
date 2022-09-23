package fp.serrano.kopykat.utils

import com.squareup.kotlinpoet.ClassName
import fp.serrano.kopykat.flattenWithSuffix

internal val ClassName.mutable: ClassName get() = flattenWithSuffix("Mutable")
internal val ClassName.dslMarker: ClassName get() = flattenWithSuffix("DslMarker")
