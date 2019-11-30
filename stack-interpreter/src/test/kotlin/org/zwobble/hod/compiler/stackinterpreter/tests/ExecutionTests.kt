package org.zwobble.hod.compiler.stackinterpreter.tests

import com.natpryce.hamkrest.assertion.assertThat
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.zwobble.hod.compiler.Module
import org.zwobble.hod.compiler.ModuleSet
import org.zwobble.hod.compiler.ast.FunctionDeclarationNode
import org.zwobble.hod.compiler.ast.Identifier
import org.zwobble.hod.compiler.ast.ModuleStatementNode
import org.zwobble.hod.compiler.backends.tests.ExecutionResult
import org.zwobble.hod.compiler.backends.tests.TestProgram
import org.zwobble.hod.compiler.backends.tests.testPrograms
import org.zwobble.hod.compiler.stackinterpreter.World
import org.zwobble.hod.compiler.stackinterpreter.executeMain
import org.zwobble.hod.compiler.stackinterpreter.loadModuleSet
import org.zwobble.hod.compiler.typechecker.CompilerError
import org.zwobble.hod.compiler.typechecker.SourceError

class ExecutionTests {
    private val disabledTests = setOf<String>(
        "ConstantField.hod",
        "symbols",
        "TailRec.hod"
    )

    @TestFactory
    fun testProgram(): List<DynamicTest> {
        return testPrograms().filter { testProgram ->
            !disabledTests.contains(testProgram.name)
        }.mapNotNull { testProgram ->
            DynamicTest.dynamicTest(testProgram.name) {
                try {
                    val modules = testProgram.load()
                    val image = loadModuleSet(modules)
                    val mainFunction = findMainFunction(modules, testProgram)
                    val world = InMemoryWorld()
                    val exitCode = executeMain(
                        mainModule = testProgram.mainModule,
                        image = image,
                        world = world
                    )

                    val executionResult = ExecutionResult(
                        exitCode = exitCode,
                        stderr = "",
                        stdout = world.stdout
                    )
                    assertThat(executionResult, testProgram.expectedResult)
                } catch (error: SourceError) {
                    print(error.source.describe())
                    throw error
                } catch (error: CompilerError) {
                    print(error.source.describe())
                    throw error
                }
            }
        }
    }

    private fun findMainFunction(modules: ModuleSet, testProgram: TestProgram): ModuleStatementNode {
        val mainModule = modules.modules.find { module ->
            module.name == testProgram.mainModule
        }!! as Module.Hod
        return mainModule.node.body.find { statement ->
            statement is FunctionDeclarationNode && statement.name == Identifier("main")
        }!!
    }
}

class InMemoryWorld : World {
    private val stdoutBuilder: StringBuilder = StringBuilder()

    override fun writeToStdout(value: String) {
        stdoutBuilder.append(value)
    }

    val stdout: String
        get() = stdoutBuilder.toString()

}
