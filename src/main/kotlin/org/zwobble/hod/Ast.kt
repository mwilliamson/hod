package org.zwobble.hod

internal interface Node {
    val source: Source
    val nodeId: Int
    val children: List<Node>
}

internal interface ExpressionNode: Node {
    interface Visitor<T> {
        fun visit(node: BoolLiteralNode): T
        fun visit(node: StringLiteralNode): T
    }

    fun <T> accept(visitor: Visitor<T>): T
}

internal data class BoolLiteralNode(
    val value: Boolean,
    override val source: Source,
    override val nodeId: Int = freshNodeId()
): ExpressionNode {
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
): ExpressionNode {
    override val children: List<Node>
        get() = listOf()

    override fun <T> accept(visitor: ExpressionNode.Visitor<T>): T {
        return visitor.visit(this)
    }
}

private var nextId = 0

private fun freshNodeId() = nextId++
