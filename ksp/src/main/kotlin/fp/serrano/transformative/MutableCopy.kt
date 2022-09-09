package fp.serrano.transformative

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.UNIT
import fp.serrano.transformative.utils.addGeneratedMarker
import fp.serrano.transformative.utils.onClassScope
import fp.serrano.transformative.utils.name

internal val KSClassDeclaration.MutableCopyKt: FileSpec
  get() = onClassScope {
    buildFile(packageName = packageName, fileName = mutableTypeName) {
      addGeneratedMarker()
      addClass(mutableClassName) {
        addTypeVariables(typeVariableNames)
        primaryConstructor {
          properties.forEach { property ->
            addParameter(property.asParameterSpec(typeParamResolver))
            addProperty(property.asPropertySpec(typeParamResolver) {
              mutable(true).initializer(property.simpleName.asString())
            })
          }
        }
      }
      addFunction(
        name = "copy",
        receiver = targetClassName,
        returns = targetClassName,
        typeVariables = typeVariableNames,
      ) {
        addModifiers(KModifier.INLINE)
        addParameter(name = "block", type = LambdaTypeName.get(receiver = mutableParameterized, returnType = UNIT))
        addCode(
          """
        | val mutable = $mutableParameterized(${properties.joinToString { "${it.name} = ${it.name}" }}).apply(block)
        | return $targetClassName(${properties.joinToString { "${it.name} = mutable.${it.name}" }})
        """.trimMargin()
        )
      }
    }
  }
