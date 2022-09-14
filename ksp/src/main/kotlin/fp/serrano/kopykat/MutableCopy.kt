@file:Suppress("WildcardImport")
package fp.serrano.kopykat

import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import fp.serrano.kopykat.utils.*

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
          addParameter(ParameterSpec(
            name = "old",
            type = targetClassName
          ))
          addProperty(PropertySpec.builder(
            name = "old",
            type = targetClassName
          ).mutable(false).initializer("old").build())
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
        val assignments = properties.map { "${it.name} = ${it.name}" } + "old = this"
        addCode(
          """
        | val mutable = $mutableParameterized(${assignments.joinToString()}).apply(block)
        | return $targetClassName(${properties.joinToString { "${it.name} = mutable.${it.name}" }})
        """.trimMargin()
        )
      }
    }
  }
