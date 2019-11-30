package org.zwobble.hod

import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.cast
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.has

internal fun isBoolLiteral(value: Boolean): Matcher<ExpressionNode> {
    return cast(has(BoolLiteralNode::value, equalTo(value)))
}
