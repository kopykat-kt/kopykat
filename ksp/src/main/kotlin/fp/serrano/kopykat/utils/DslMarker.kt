package fp.serrano.kopykat.utils

import com.squareup.kotlinpoet.KModifier
import fp.serrano.kopykat.addClass

internal val TypeCompileScope.annotationClassName get() = className("${target.simpleName}CopyDslMarker")

internal fun FileCompilerScope.addDslMarkerClass() {
  with(file) {
    addClass(annotationClassName) {
      addAnnotation(DslMarker::class)
      addModifiers(KModifier.ANNOTATION)
    }
  }
}