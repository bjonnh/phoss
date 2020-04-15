package dataset

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


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
    val spectra: MutableList<Spectrum> = mutableListOf()
)

data class PHOSSDataset(
    val directory: Path,
    val code: Code,
) {
    private val datasetEntries = DatasetEntries()

    var fileName: String? = null
        private set
    private var zipFile: ZipOutputStream? = null
    private var zipFileStream: FileOutputStream? = null

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
            require(zipFile == null && zipFileStream == null) { "Directory cannot be changed once the archive is opened." }
            field = value
        }

    fun openArchive() {
        this.fileName = "${this.outputDirectory}/${this.code}.zip"
        this.zipFileStream = FileOutputStream(this.fileName!!)
        this.zipFile = ZipOutputStream(this.zipFileStream)
    }

    fun closeArchive() {
        this.zipFile?.close()
        this.zipFileStream?.close()
    }

    fun addDatasetReference(reference: DatasetReference) {
        this.datasetReferenceStore.add(reference)
    }

    fun addEntry(pathInArchive: String, content: String) {
        require(this.zipFile != null)
        val relativePath = this.directory.relativize(Path.of(pathInArchive))
        val entry = ZipEntry(relativePath.toString())
        this.zipFile?.putNextEntry(entry)
        this.zipFile?.write(content.toByteArray())
    }

    fun addFile(pathInArchive: String, sourcePath: Path) {
        this.addFile(pathInArchive,
            FileInputStream(sourcePath.toFile()))
    }

    fun addFile(pathInArchive: String, sourceFileName: String) {
        this.addFile(pathInArchive,
            FileInputStream(File(sourceFileName)))
    }

    fun addFile(pathInArchive: String, inputStream: InputStream) {
        require(this.zipFile != null)
        val relativePath = this.directory.relativize(Path.of(pathInArchive))
        val entry = ZipEntry(relativePath.toString())
        this.zipFile?.putNextEntry(entry)
        val bytes = ByteArray(1024)
        var length: Int
        while (inputStream.read(bytes).also { length = it } >= 0) {
            this.zipFile?.write(bytes, 0, length)
        }
        inputStream.close()
    }

    fun addName(name: String) = this.mutableNames.add(name)
    fun addNames(names: List<String>) = names.map { this.addName(it) }

    fun addMolecule(molecule: Molecule) =this.datasetEntries.molecules.add(molecule)
    fun addSpectrum(spectrum: Spectrum) =this.datasetEntries.spectra.add(spectrum)
}