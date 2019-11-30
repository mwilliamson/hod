package org.zwobble.hod.compiler.tests.types

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test
import org.zwobble.hod.compiler.types.AnonymousUnionType
import org.zwobble.hod.compiler.types.IntType
import org.zwobble.hod.compiler.types.StringType

class AnonymousUnionTests {
    @Test
    fun anonymousUnionShortDescriptionContainsAllMembers() {
        val type = AnonymousUnionType(members = listOf(IntType, StringType))

        assertThat(type.shortDescription, equalTo("Int | String"))
    }
}
