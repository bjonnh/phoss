package dataset

import helpers.createInputStreamSupplier
import mu.KotlinLogging
import org.apache.commons.compress.archivers.zip.ParallelScatterZipCreator
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.nio.file.Path
import java.util.zip.ZipEntry


data class DatasetReference(
    val name: String,
)

data class OntologicalTerm(
    val subject: String,
    val predicate: String,
    val obj: String,
)


data class DatasetEntries(
    val molecules: MutableList<Molecule> = mutableListOf(),
    val spectra: MutableList<Spectrum> = mutableListOf(),
)

class Archive(fileName: String, var fastMode: Boolean) {
    val zos: ZipArchiveOutputStream = ZipArchiveOutputStream(File(fileName))
    val zipCreator: ParallelScatterZipCreator = ParallelScatterZipCreator()

    fun addEntry(pathInArchive: String, content: InputStream) {
        val entry = ZipArchiveEntry(pathInArchive)
        entry.method = if (fastMode) {
            ZipEntry.STORED
        } else {
            ZipEntry.DEFLATED
        }
        this.zipCreator.addArchiveEntry(entry, createInputStreamSupplier(content))
    }

    fun close() {
        zipCreator.writeTo(zos)
        zos.close()
    }
}

data class PHOSSDataset(
    val directory: Path,
    val code: Code,
) {
    private val datasetEntries = DatasetEntries()
    val logger = KotlinLogging.logger {}
    var fileName: String? = null
        private set

    var archive: Archive? = null

    private val datasetReferenceStore: MutableList<DatasetReference> = mutableListOf()
    private var depiction: Any? = null
    private val ontologyStore: MutableList<OntologicalTerm> = mutableListOf()

    private val mutableNames: MutableSet<String> = mutableSetOf()

    val names: Set<String>
        get() {
            return setOf(*this.mutableNames.toTypedArray())
        }

    var outputDirectory: Path = Path.of(".")
        set(value) {
            require(archive == null) { "Directory cannot be changed once the archive is opened." }
            field = value
        }

    fun openArchive(fastMode: Boolean = false) {
        this.fileName = "${this.outputDirectory}/${this.code}.zip"
        this.archive = Archive(this.fileName!!, fastMode)
    }

    fun closeArchive() {
        this.archive?.close()
    }

    fun addDatasetReference(reference: DatasetReference) {
        this.datasetReferenceStore.add(reference)
    }

    fun addEntry(pathInArchive: String, content: InputStream) {
        require(this.archive != null)
        this.archive?.addEntry(pathInArchive, content)
    }

    fun addEntry(pathInArchive: String, content: String) {
        this.addEntry(pathInArchive, IOUtils.toInputStream(content))
    }

    fun addFile(pathInArchive: String, sourcePath: Path) {
        val relativePath = this.directory.relativize(Path.of(pathInArchive)).toString()
        this.addEntry(relativePath,
            FileInputStream(sourcePath.toFile()))
    }

    fun addName(name: String) = this.mutableNames.add(name)
    fun addNames(names: List<String>) = names.map { this.addName(it) }

    fun addMolecule(molecule: Molecule) = this.datasetEntries.molecules.add(molecule)
    fun addSpectrum(spectrum: Spectrum) = this.datasetEntries.spectra.add(spectrum)
}