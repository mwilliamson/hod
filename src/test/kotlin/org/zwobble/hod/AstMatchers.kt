package org.zwobble.hod

import com.natpryce.hamkrest.*

internal fun isBoolLiteral(value: Boolean): Matcher<ExpressionNode> {
    return cast(has(BoolLiteralNode::value, equalTo(value)))
}

internal fun isCompilationUnit(imports: Matcher<List<ImportNode>>): Matcher<CompilationUnitNode> {
    return allOf(
        has(CompilationUnitNode::imports, imports)
    )
}

internal fun isImport(target: Matcher<String>, path: Matcher<String>): Matcher<ImportNode> {
    return allOf(
        has(ImportNode::target, target),
        has(ImportNode::path, path)
    )
}

internal fun isReference(name: String): Matcher<ExpressionNode> {
    return cast(has(ReferenceNode::name, equalTo(name)))
}

internal fun isStringLiteral(value: String): Matcher<ExpressionNode> {
    return cast(has(StringLiteralNode::value, equalTo(value)))
}
