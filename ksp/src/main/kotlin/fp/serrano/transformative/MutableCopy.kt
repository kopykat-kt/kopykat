package fp.serrano.transformative

import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.TypeParameterResolver


internal fun mutableCopy(
  packageName: String,
  mutableTypeName: String,
  mutableClassName: ClassName,
  typeVariables: List<TypeVariableName>,
  properties: Sequence<KSPropertyDeclaration>,
  typeParamResolver: TypeParameterResolver,
  targetClassName: TypeName
) = buildFile(packageName = packageName, fileName = mutableTypeName) {
  addClass(mutableClassName) {
    addTypeVariables(typeVariables)
    primaryConstructor {
      properties.forEach { property ->
        addParameter(property.asParameterSpec(typeParamResolver))
        addProperty(property.asPropertySpec(typeParamResolver) {
          mutable(true).initializer(property.simpleName.asString())
        })
      }
    }
  }
  val mutableParameterized = mutableClassName.parameterizedWhenNotEmpty(typeVariables)
  addFunction(
    name = "copy",
    receiver = targetClassName,
    returns = targetClassName,
    typeVariables = typeVariables,
  ) {
    addParameter(name = "block", type = LambdaTypeName.get(receiver = mutableParameterized, returnType = UNIT))
    addCode(
      """
            | val mutable = $mutableParameterized(${properties.joinToString { "${it.name} = ${it.name}" }}).apply(block)
            | return $targetClassName(${properties.joinToString { "${it.name} = mutable.${it.name}" }})
            """.trimMargin()
    )
  }
}

private val KSDeclaration.name get() = simpleName.asString()
