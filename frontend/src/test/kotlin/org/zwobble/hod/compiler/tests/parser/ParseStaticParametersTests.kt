package org.zwobble.hod.compiler.tests.parser

import com.natpryce.hamkrest.assertion.assertThat
import org.junit.jupiter.api.Test
import org.zwobble.hod.compiler.parser.TokenIterator
import org.zwobble.hod.compiler.parser.TokenType
import org.zwobble.hod.compiler.parser.parseStaticParameters
import org.zwobble.hod.compiler.tests.*

class ParseStaticParametersTests {
    @Test
    fun canParseSingleInvariantTypeParameter() {
        val parameters = parseString(Companion::parseStaticParametersWithVariance, "[T]")
        assertThat(parameters, isSequence(
            isTypeParameter(name = isIdentifier("T"), variance = isInvariant)
        ))
    }

    @Test
    fun canParseSingleCovariantTypeParameter() {
        val parameters = parseString(Companion::parseStaticParametersWithVariance, "[+T]")
        assertThat(parameters, isSequence(
            isTypeParameter(name = isIdentifier("T"), variance = isCovariant)
        ))
    }

    @Test
    fun canParseSingleContravariantTypeParameter() {
        val parameters = parseString(Companion::parseStaticParametersWithVariance, "[-T]")
        assertThat(parameters, isSequence(
            isTypeParameter(name = isIdentifier("T"), variance = isContravariant)
        ))
    }

    @Test
    fun canParseEffectParameter() {
        val parameters = parseString(Companion::parseStaticParametersWithVariance, "[!E]")
        assertThat(parameters, isSequence(
            isEffectParameterNode(name = isIdentifier("E"))
        ))
    }

    companion object {
        private fun parseStaticParametersWithVariance(tokens: TokenIterator<TokenType>)
            = parseStaticParameters(allowVariance = true, tokens = tokens)
    }
}
