package org.zwobble.hod.compiler

import org.zwobble.hod.compiler.ast.ReferenceNode
import org.zwobble.hod.compiler.ast.VariableBindingNode

interface ResolvedReferences {
    operator fun get(node: ReferenceNode): VariableBindingNode
}
