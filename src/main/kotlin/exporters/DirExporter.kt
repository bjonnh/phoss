package exporters

import org.apache.commons.io.FileUtils
import java.io.InputStream
import java.nio.file.Path

/**
 * A DirExporter exports in a directory on the given Path.
 *
 * All entries will have their directory processed and cannot
 * be absolute.
 *
 * @param path Location of the output directory
 */
class DirExporter(var path: Path): IExporter {
    private var outputPath: Path? = null
    override fun open(code: String) {
        outputPath = path.resolve(code)
        outputPath?.toFile()?.mkdir()
    }

    override fun addEntry(pathInArchive: String, content: InputStream) {
        val filePath = outputPath?.resolve(pathInArchive) ?: throw Exception("Invalid Path")
        filePath.parent.toFile().mkdirs()
        FileUtils.copyInputStreamToFile(content, filePath.toFile())
    }

    override fun close() {}
}