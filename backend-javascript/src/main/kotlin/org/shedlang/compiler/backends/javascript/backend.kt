package org.shedlang.compiler.backends.javascript

import org.shedlang.compiler.FrontEndResult
import org.shedlang.compiler.Module
import org.shedlang.compiler.backends.Backend
import java.io.File
import java.io.Writer
import java.nio.charset.StandardCharsets
import java.nio.file.Path

val backend = object: Backend {
    override fun compile(frontEndResult: FrontEndResult, target: Path) {
        frontEndResult.modules.forEach({ module ->
            val modulePath = modulePath(module.path)
            val destination = target.resolve(modulePath)
            destination.parent.toFile().mkdirs()
            destination.toFile().writer(StandardCharsets.UTF_8).use { writer ->
                compileModule(
                    module = module,
                    writer = writer
                )
            }
        })
    }
}

fun compile(frontendResult: FrontEndResult, target: Path) {
    backend.compile(frontendResult, target = target)
}

private fun modulePath(path: List<String>) = path.joinToString(File.separator) + ".js"

private fun compileModule(module: Module, writer: Writer) {
    val generateCode = generateCode(module.node)
    val contents = stdlib + serialise(generateCode) + "\n"
    writer.write(contents)
    if (module.hasMain()) {
        writer.write("""
            if (require.main === module) {
                main();
            }
        """.trimIndent())
    }
}

private val stdlib = """
    function intToString(value) {
        return value.toString();
    }

    function print(value) {
        console.log(value);
    }

    function declareShape(name) {
        return {
            name: name,
            typeId: freshTypeId()
        };
    }

    var nextTypeId = 1;
    function freshTypeId() {
        return nextTypeId++;
    }

    function isType(value, type) {
        return value != null && value.${"$"}shedType === type;
    }

    var ${"$"}shed = {
        declareShape: declareShape,
        isType: isType
    };
"""