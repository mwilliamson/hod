package org.zwobble.hod.compiler

import org.zwobble.hod.compiler.ast.Identifier
import org.zwobble.hod.compiler.ast.ImportPath
import org.zwobble.hod.compiler.ast.ImportPathBase


fun resolveImport(importingModuleName: List<Identifier>, importPath: ImportPath): List<Identifier> {
    return when (importPath.base) {
        ImportPathBase.Relative -> {
            importingModuleName.dropLast(1) + importPath.parts
        }
        ImportPathBase.Absolute -> {
            importPath.parts
        }
    }
}
