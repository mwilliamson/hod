package org.zwobble.hod

internal interface Node {
    val source: Source
    val nodeId: Int
    val children: List<Node>
}


internal data class CompilationUnitNode(
    val imports: List<ImportNode>,
    override val source: Source,
    override val nodeId: Int = freshNodeId()
) : Node {
    override val children: List<Node>
        get() = imports
}

internal data class ImportNode(
    val target: String,
    val path: String,
    override val source: Source,
    override val nodeId: Int = freshNodeId()
) : Node {
    override val children: List<Node>
        get() = listOf()
}

internal interface ExpressionNode : Node {
    interface Visitor<T> {
        fun visit(node: BoolLiteralNode): T
        fun visit(node: ReferenceNode): T
        fun visit(node: StringLiteralNode): T
    }

    fun <T> accept(visitor: Visitor<T>): T
}

internal data class BoolLiteralNode(
    val value: Boolean,
    override val source: Source,
    override val nodeId: Int = freshNodeId()
) : ExpressionNode {
    override val children: List<Node>
        get() = listOf()

    override fun <T> accept(visitor: ExpressionNode.Visitor<T>): T {
        return visitor.visit(this)
    }
}

internal data class ReferenceNode(
    val name: String,
    override val source: Source,
    override val nodeId: Int = freshNodeId()
) : ExpressionNode {
    override val children: List<Node>
        get() = listOf()

    override fun <T> accept(visitor: ExpressionNode.Visitor<T>): T {
        return visitor.visit(this)
    }
}

internal data class StringLiteralNode(
    val value: String,
    override val source: Source,
    override val nodeId: Int = freshNodeId()
) : ExpressionNode {
    override val children: List<Node>
        get() = listOf()

    override fun <T> accept(visitor: ExpressionNode.Visitor<T>): T {
        return visitor.visit(this)
    }
}

private var nextId = 0

private fun freshNodeId() = nextId++
