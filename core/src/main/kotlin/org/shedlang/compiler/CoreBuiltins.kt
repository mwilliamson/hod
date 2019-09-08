package org.shedlang.compiler

import org.shedlang.compiler.ast.CallBaseNode
import org.shedlang.compiler.ast.ReferenceNode
import org.shedlang.compiler.ast.builtinVariable
import org.shedlang.compiler.types.CastType

val castBuiltin = builtinVariable("cast", CastType)

fun isCast(call: CallBaseNode, references: ResolvedReferences): Boolean {
    val receiver = call.receiver
    return receiver is ReferenceNode && references[receiver] == castBuiltin
}