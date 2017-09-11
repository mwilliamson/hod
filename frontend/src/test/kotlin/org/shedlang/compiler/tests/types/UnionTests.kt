package org.shedlang.compiler.tests.types

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.cast
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test
import org.shedlang.compiler.tests.*
import org.shedlang.compiler.types.BoolType
import org.shedlang.compiler.types.IntType
import org.shedlang.compiler.types.StringType
import org.shedlang.compiler.types.union

class UnionTests {
    @Test
    fun unionIsLeftWhenRightCanBeCoercedToLeft() {
        val left = unionType("T", listOf(IntType, StringType))
        val right = IntType
        val union = union(left, right)
        assertThat(union, cast(equalTo(left)))
    }

    @Test
    fun unionIsRightWhenLeftCanBeCoercedToRight() {
        val left = IntType
        val right = unionType("T", listOf(IntType, StringType))
        val union = union(left, right)
        assertThat(union, cast(equalTo(right)))
    }

    @Test
    fun unionIsUnionWhenLeftAndRightCannotBeCoercedToEachOther() {
        val left = IntType
        val right = StringType
        val union = union(left, right)
        assertThat(union, isUnionType(members = isSequence(isIntType, isStringType)))
    }

    @Test
    fun repeatedUnionsFromLeftProduceSingleUnion() {
        val union = union(union(IntType, StringType), BoolType)
        assertThat(union, isUnionType(members = isSequence(isIntType, isStringType, isBoolType)))
    }

    @Test
    fun repeatedUnionsFromRightProduceSingleUnion() {
        val union = union(IntType, union(StringType, BoolType))
        assertThat(union, isUnionType(members = isSequence(isIntType, isStringType, isBoolType)))
    }
}
