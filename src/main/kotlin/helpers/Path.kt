package helpers

import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes


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

/**
 * Find a file with the matching glob in the given location
 *
 * a glob can be glob:\*\*\/\*.jdf  for finding jdf files recursively
 */
fun Path.findFileMatch(glob: String?): List<Path> {
    val matchingPaths = mutableListOf<Path>()
    val pathMatcher = FileSystems.getDefault().getPathMatcher(glob)
    Files.walkFileTree(this, object : SimpleFileVisitor<Path>() {
        override fun visitFile(
            path: Path,
            attrs: BasicFileAttributes,
        ): FileVisitResult {
            if (pathMatcher.matches(path)) {
                matchingPaths.add(path)
            }
            return FileVisitResult.CONTINUE
        }

        override fun visitFileFailed(file: Path, exc: IOException): FileVisitResult {
            return FileVisitResult.CONTINUE
        }
    })
    return matchingPaths
}