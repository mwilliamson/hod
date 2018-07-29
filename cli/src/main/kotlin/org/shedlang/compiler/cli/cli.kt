package org.shedlang.compiler.cli

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import com.xenomachina.argparser.mainBody
import org.shedlang.compiler.ast.Identifier
import org.shedlang.compiler.backends.Backend
import org.shedlang.compiler.interpreter.fullyEvaluate
import org.shedlang.compiler.readPackage
import org.shedlang.compiler.typechecker.SourceError
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.system.exitProcess

object ShedCli {
    @JvmStatic
    fun main(rawArguments: Array<String>) {
        val exitCode = mainBody {
            run(rawArguments)
        }
        System.exit(exitCode)
    }

    private fun run(rawArguments: Array<String>): Int {
        val arguments = Arguments(ArgParser(rawArguments))
        val mainName = arguments.mainModule.split(".")

        val tempDir = createTempDir()
        try {
            val base = Paths.get(arguments.source)
            val backend = arguments.backend
            if (backend == null) {
                val source = readPackage(base, arguments.mainModule)
                val result = fullyEvaluate(source, readMainModuleName(arguments.mainModule))
                // TODO: print stdout as it's generated
                print(result.stdout)
                return result.exitCode
            } else {
                compile(
                    base = base,
                    mainName = arguments.mainModule,
                    backend = backend,
                    target = tempDir.toPath()
                )
                return backend.run(tempDir.toPath(), mainName)
            }
        } finally {
            tempDir.deleteRecursively()
        }
    }

    private class Arguments(parser: ArgParser) {
        val source by parser.shedSource()
        val mainModule by parser.positional("MAIN", help = "main module to run")
        val backend by parser.shedBackends().default(null)

        init {
            parser.force()
        }
    }
}

object ShedcCli {
    @JvmStatic
    fun main(rawArguments: Array<String>) {
        return mainBody {
            run(rawArguments)
        }
    }

    private fun run(rawArguments: Array<String>) {
        val arguments = Arguments(ArgParser(rawArguments))
        compile(
            base = Paths.get(arguments.source),
            mainName = arguments.mainModule,
            backend = arguments.backend,
            target = Paths.get(arguments.outputPath)
        )
    }

    private class Arguments(parser: ArgParser) {
        val source by parser.shedSource()
        val mainModule by parser.positional("MAIN", help = "main module to run")
        val outputPath by parser.storing("--output-path",   "-o", help = "path to output directory")
        val backend by parser.shedBackends()

        init {
            parser.force()
        }
    }
}

private fun compile(base: Path, mainName: String, backend: Backend, target: Path) {
    try {
        val result = readPackage(base, mainName)
        backend.compile(result, target = target)
    } catch (error: SourceError) {
        System.err.println("Error: " + error.message)
        System.err.println(error.source.describe())
        exitProcess(2)
    }
}

private fun readPackage(base: Path, mainName: String) =
    readPackage(base, readMainModuleName(mainName))

private fun readMainModuleName(mainName: String) =
    mainName.split(".").map(::Identifier)
