package org.zwobble.hod

import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.cast
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.has

internal fun isBoolLiteral(value: Boolean): Matcher<ExpressionNode> {
    return cast(has(BoolLiteralNode::value, equalTo(value)))
}

internal fun isReference(name: String): Matcher<ExpressionNode> {
    return cast(has(ReferenceNode::name, equalTo(name)))
}

internal fun isStringLiteral(value: String): Matcher<ExpressionNode> {
    return cast(has(StringLiteralNode::value, equalTo(value)))
}
