package org.zwobble.hod.compiler.backends.javascript.tests

import com.natpryce.hamkrest.assertion.assertThat
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.zwobble.hod.compiler.ast.Identifier
import org.zwobble.hod.compiler.backends.javascript.compile
import org.zwobble.hod.compiler.backends.tests.run
import org.zwobble.hod.compiler.backends.tests.temporaryDirectory
import org.zwobble.hod.compiler.backends.tests.testPrograms
import org.zwobble.hod.compiler.typechecker.CompilerError
import org.zwobble.hod.compiler.typechecker.SourceError

class ExecutionTests {
    private val disabledTests = setOf<String>("TailRec.hod")

    @TestFactory
    fun testProgram(): List<DynamicTest> {
        return testPrograms().filter { testProgram ->
            !disabledTests.contains(testProgram.name)
        }.map({ testProgram -> DynamicTest.dynamicTest(testProgram.name, {
            try {
                temporaryDirectory().use { temporaryDirectory ->
                    compile(testProgram.load(), target = temporaryDirectory.file.toPath())
                    val mainJsModule = "./" + testProgram.mainModule.map(Identifier::value).joinToString("/") + ".js"
                    val result = run(
                        listOf("node", mainJsModule),
                        workingDirectory = temporaryDirectory.file
                    )
                    assertThat("stdout was:\n" + result.stdout + "\nstderr was:\n" + result.stderr, result, testProgram.expectedResult)
                }
            } catch (error: SourceError) {
                print(error.source.describe())
                throw error
            } catch (error: CompilerError) {
                print(error.source.describe())
                throw error
            }
        }) })
    }
}
