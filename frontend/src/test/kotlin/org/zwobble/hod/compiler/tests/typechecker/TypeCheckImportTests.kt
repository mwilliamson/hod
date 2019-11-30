package org.zwobble.hod.compiler.tests.typechecker

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.cast
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.has
import org.junit.jupiter.api.Test
import org.zwobble.hod.compiler.Module
import org.zwobble.hod.compiler.ModuleResult
import org.zwobble.hod.compiler.ast.Identifier
import org.zwobble.hod.compiler.ast.ImportPath
import org.zwobble.hod.compiler.frontend.tests.throwsException
import org.zwobble.hod.compiler.tests.import
import org.zwobble.hod.compiler.tests.isIdentifier
import org.zwobble.hod.compiler.tests.isSequence
import org.zwobble.hod.compiler.tests.targetVariable
import org.zwobble.hod.compiler.typechecker.ModuleNotFoundError
import org.zwobble.hod.compiler.typechecker.MultipleModulesWithSameNameFoundError
import org.zwobble.hod.compiler.typechecker.typeCheck
import org.zwobble.hod.compiler.types.ModuleType

class TypeCheckImportTests {
    @Test
    fun importIntroducesModuleIntoScope() {
        val path = ImportPath.relative(listOf("Messages"))
        val target = targetVariable("M")
        val node = import(
            target = target,
            path = path
        )
        val moduleType = ModuleType(fields = mapOf())
        val typeContext = typeContext(
            modules = mapOf(
                path to ModuleResult.Found(Module.Native(
                    type = moduleType,
                    name = identifiers("Messages")
                ))
            )
        )
        typeCheck(node, typeContext)
        assertThat(typeContext.typeOf(target), cast(equalTo(moduleType)))
    }

    @Test
    fun whenModuleIsNotFoundThenErrorIsThrown() {
        val path = ImportPath.relative(listOf("Messages"))
        val node = import(name = Identifier("M"), path = path)
        val typeContext = typeContext(modules = mapOf(path to ModuleResult.NotFound(name = identifiers("Lib", "Messages"))))

        assertThat(
            { typeCheck(node, typeContext) },
            throwsException(has(ModuleNotFoundError::name, isSequence(isIdentifier("Lib"), isIdentifier("Messages"))))
        )
    }

    @Test
    fun whenMultipleModulesAreNotFoundThenErrorIsThrown() {
        val path = ImportPath.relative(listOf("Messages"))
        val node = import(name = Identifier("M"), path = path)
        val typeContext = typeContext(modules = mapOf(path to ModuleResult.FoundMany(name = identifiers("Lib", "Messages"))))

        assertThat(
            { typeCheck(node, typeContext) },
            throwsException(has(MultipleModulesWithSameNameFoundError::name, isSequence(isIdentifier("Lib"), isIdentifier("Messages"))))
        )
    }

    private fun identifiers(vararg names: String) = names.map(::Identifier).toList()
}
