package fp.serrano.kopykat.utils

import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.asTypeName

internal fun FileCompilerScope.addGeneratedMarker() {
  file.addProperty(
    PropertySpec.builder(MARKER, UnitTypeName).addModifiers(PRIVATE).initializer("Unit").build()
  )
}

internal fun KSFile.hasGeneratedMarker(): Boolean =
  declarations.filterIsInstance<KSPropertyDeclaration>().any { it.baseName == MARKER }

private const val MARKER = "transformativeGenerated"
private val UnitTypeName = Unit::class.asTypeName()
