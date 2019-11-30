package org.zwobble.hod.compiler.tests.types

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test
import org.zwobble.hod.compiler.types.contravariantTypeParameter
import org.zwobble.hod.compiler.types.covariantTypeParameter
import org.zwobble.hod.compiler.types.invariantTypeParameter

class TypeParameterTests {
    @Test
    fun shortDescriptionOfInvariantTypeParameterIsNameOfTypeParameter() {
        val typeParameter = invariantTypeParameter("T")
        assertThat(typeParameter.shortDescription, equalTo("T"))
    }

    @Test
    fun shortDescriptionOfCovariantTypeParameterPrefixesNameWithPlus() {
        val typeParameter = covariantTypeParameter("T")
        assertThat(typeParameter.shortDescription, equalTo("+T"))
    }

    @Test
    fun shortDescriptionOfContravariantTypeParameterPrefixesNameWithMinus() {
        val typeParameter = contravariantTypeParameter("T")
        assertThat(typeParameter.shortDescription, equalTo("-T"))
    }
}
