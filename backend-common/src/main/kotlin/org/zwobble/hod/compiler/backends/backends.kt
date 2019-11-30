package org.zwobble.hod.compiler.backends

import org.zwobble.hod.compiler.ModuleSet
import java.nio.file.Path

interface Backend {
    fun compile(moduleSet: ModuleSet, target: Path): Unit
    fun run(path: Path, module: List<String>): Int
}
