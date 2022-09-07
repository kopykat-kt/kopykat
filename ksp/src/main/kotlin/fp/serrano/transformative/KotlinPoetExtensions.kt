package fp.serrano.transformative

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

public fun FileSpec.Builder.addFunction(
  name: String,
  receiver: TypeName? = null,
  returns: TypeName? = null,
  typeVariables: Iterable<TypeVariableName> = emptyList(),
  block: FunSpec.Builder.() -> Unit = {},
) {
  addFunction(FunSpec.builder(name).apply {
    receiver?.apply { receiver(receiver) }
    returns?.apply { returns(returns) }
    addTypeVariables(typeVariables)
  }.apply(block).build())
}

public fun buildFile(packageName: String, fileName: String, block: FileSpec.Builder.() -> Unit): FileSpec =
  FileSpec.builder(packageName, fileName).apply(block).build()

public fun ClassName.parameterizedWhenNotEmpty(
  typeArguments: List<TypeName>
): TypeName = takeIf { typeArguments.isNotEmpty() }?.parameterizedBy(typeArguments) ?: this
