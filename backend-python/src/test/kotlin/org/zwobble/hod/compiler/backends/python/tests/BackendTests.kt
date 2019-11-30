package org.zwobble.hod.compiler.backends.python.tests

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test
import org.zwobble.hod.compiler.EMPTY_TYPES
import org.zwobble.hod.compiler.Module
import org.zwobble.hod.compiler.ModuleSet
import org.zwobble.hod.compiler.ast.Identifier
import org.zwobble.hod.compiler.backends.python.hodModuleNameToPythonModuleName
import org.zwobble.hod.compiler.backends.python.topLevelPythonPackageName
import org.zwobble.hod.compiler.tests.module
import org.zwobble.hod.compiler.tests.moduleType
import org.zwobble.hod.compiler.typechecker.ResolvedReferencesMap

class BackendTests {
    @Test
    fun moduleNameIsConvertedToModuleUnderShedPackage() {
        val pythonName = hodModuleNameToPythonModuleName(
            moduleName = listOf(Identifier("X"), Identifier("Y")),
            moduleSet = moduleSetWithNames(listOf(listOf("X", "Y")))
        )
        assertThat(pythonName, equalTo(listOf(topLevelPythonPackageName, "X", "Y")))
    }

    @Test
    fun packageIsConvertedToInitModuleUnderShedPackage() {
        val pythonName = hodModuleNameToPythonModuleName(
            moduleName = listOf(Identifier("X"), Identifier("Y")),
            moduleSet = moduleSetWithNames(listOf(listOf("X", "Y", "Z")))
        )
        assertThat(pythonName, equalTo(listOf(topLevelPythonPackageName, "X", "Y", "__init__")))
    }

    private fun moduleSetWithNames(moduleNames: List<List<String>>): ModuleSet {
        return ModuleSet(modules = moduleNames.map { moduleName ->
            Module.Hod(
                name = moduleName.map(::Identifier),
                node = module(),
                references = ResolvedReferencesMap.EMPTY,
                type = moduleType(),
                types = EMPTY_TYPES
            )
        })
    }
}
