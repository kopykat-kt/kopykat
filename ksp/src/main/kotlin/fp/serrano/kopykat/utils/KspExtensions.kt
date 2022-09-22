package fp.serrano.kopykat.utils

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.UNIT

internal val KSDeclaration.name get() = simpleName.asString()

internal val KSClassDeclaration.sealedTypes get() = getSealedSubclasses()

internal fun TypeName.asReceiverConsumer() = LambdaTypeName.get(receiver = this, returnType = UNIT)