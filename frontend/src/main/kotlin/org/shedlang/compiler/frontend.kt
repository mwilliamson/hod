package org.shedlang.compiler

import org.shedlang.compiler.ast.FunctionDeclarationNode
import org.shedlang.compiler.ast.ImportPath
import org.shedlang.compiler.ast.ImportPathBase
import org.shedlang.compiler.ast.ModuleNode
import org.shedlang.compiler.parser.parse
import org.shedlang.compiler.typechecker.ResolvedReferences
import org.shedlang.compiler.typechecker.resolve
import org.shedlang.compiler.typechecker.typeCheck
import org.shedlang.compiler.types.ModuleType
import java.io.File
import java.nio.file.Path
import java.util.*


class FrontEndResult(
    val modules: List<Module>
)

class Module(
    val path: List<String>,
    val node: ModuleNode,
    val type: ModuleType,
    val references: ResolvedReferences
) {
    fun hasMain() = node.body.any({ node ->
        node is FunctionDeclarationNode && node.name == "main"
    })
}

fun read(base: Path, path: Path): FrontEndResult {
    return readAll(base, listOf(path))
}

fun readDirectory(base: Path): FrontEndResult {
    if (base.toFile().isFile) {
        return read(base = base.parent, path = base.fileName)
    } else {
        val paths = base.toFile()
            .walk()
            .filter({ file -> file.path.endsWith(".shed") })
            .map({ file -> base.relativize(file.toPath()) })

        return readAll(base, paths = paths.asIterable())
    }
}

private fun readAll(base: Path, paths: Iterable<Path>): FrontEndResult {
    val modules = HashMap<Path, Module>()

    fun getModule(path: Path): Module {
        return modules.computeIfAbsent(path, { path ->
            readModule(base = base, relativePath = path, getModule = ::getModule)
        })
    }

    paths.forEach({ path -> getModule(path) })

    return FrontEndResult(modules = modules.values.toList())
}



private fun readModule(base: Path, relativePath: Path, getModule: (Path) -> Module): Module {
    val path = base.resolve(relativePath)
    val moduleNode = parse(filename = path.toString(), input = path.toFile().readText())

    val resolvedReferences = resolve(
        moduleNode,
        builtins.associate({ builtin -> builtin.name to builtin.nodeId})
    )

    val typeCheckResult = typeCheck(
        moduleNode,
        nodeTypes = builtins.associate({ builtin -> builtin.nodeId to builtin.type}),
        resolvedReferences = resolvedReferences,
        getModule = { importPath ->
            val result = getModule(resolveModule(relativePath, importPath))
            result.type
        }
    )

    return Module(
        path = identifyModule(relativePath),
        node = moduleNode,
        type = typeCheckResult.moduleType,
        references = resolvedReferences
    )
}

fun identifyModule(path: Path): List<String> {
    val pathParts = path.map(Path::toString)
    return pathParts.take(pathParts.size - 1) + pathParts.last().removeSuffix(".shed")
}

fun resolveModule(path: Path, importPath: ImportPath): Path {
    // TODO: test this properly
    val base = when (importPath.base) {
        ImportPathBase.Absolute -> throw UnsupportedOperationException()
        is ImportPathBase.Relative -> path
    }

    return base.resolveSibling(importPath.parts.joinToString(File.separator) + ".shed")
}
