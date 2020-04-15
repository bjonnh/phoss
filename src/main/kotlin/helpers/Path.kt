package helpers

import java.nio.file.Path

fun Path.recursiveApply(function: (Path) -> Unit) {
    val filesList = this.toFile().listFiles()
    if (filesList != null) for (fil in filesList) {
        if (fil.isDirectory) {
            fil.toPath().recursiveApply(function)
        } else {
            function(fil.toPath())
        }
    }
}

fun Path.findFile(name: String, recursive: Boolean = true): List<Path> {
    val matchingPaths = mutableListOf<Path>()
    val filesList = this.toFile().listFiles()
    if (filesList != null) for (fil in filesList) {
        if (fil.isDirectory) {
            if (recursive)
                matchingPaths.addAll(fil.toPath().findFile(name, true))
        } else if (name.equals(fil.name, ignoreCase = true)) {
            matchingPaths.add(fil.toPath())
        }
    }
    return matchingPaths
}