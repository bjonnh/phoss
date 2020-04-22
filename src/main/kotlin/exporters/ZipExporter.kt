package exporters

import helpers.createInputStreamSupplier
import org.apache.commons.compress.archivers.zip.ParallelScatterZipCreator
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import java.io.InputStream
import java.nio.file.Path
import java.util.zip.ZipEntry

/**
 * An exporter that writes to a zip file.
 *
 * The name of the zipfile is set in the open method.
 *
 * @param path Where the zip file will be written
 * @param fastMode If true, write the zipfile with no compression
 */
class ZipExporter(var path: Path, var fastMode: Boolean): IExporter {
    private var zos: ZipArchiveOutputStream? = null
    private val zipCreator: ParallelScatterZipCreator = ParallelScatterZipCreator()

    override fun open(code: String) {
        require(zos == null) { "Cannot open this exporter twice." }
        val fileName = path.resolve("${code}.zip")
        zos = ZipArchiveOutputStream(fileName.toFile())
    }

    override fun addEntry(pathInArchive: String, content: InputStream) {
        val entry = ZipArchiveEntry(pathInArchive)
        entry.method = if (fastMode) {
            ZipEntry.STORED
        } else {
            ZipEntry.DEFLATED
        }
        this.zipCreator.addArchiveEntry(entry, createInputStreamSupplier(content))
    }

    override fun close() {
        require (zos != null) { "Trying to close a non opened exporter" }
        zipCreator.writeTo(zos)
        zos?.close()
    }
}
