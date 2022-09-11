package fp.serrano.kopykat.utils

import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.asTypeName

internal fun FileSpec.Builder.addGeneratedMarker() {
  addProperty(PropertySpec.builder(Marker, Unit::class.asTypeName()).addModifiers(PRIVATE).initializer("Unit").build())
}

internal fun KSFile.hasGeneratedMarker(): Boolean =
  declarations.filterIsInstance<KSPropertyDeclaration>().any { it.name == Marker }

private const val Marker = "transformativeGenerated"
