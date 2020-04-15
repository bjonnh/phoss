import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


data class DatasetReference(
    val name: String
)

data class OntologicalTerm(
    val subject: String,
    val predicate: String,
    val obj: String
)

class BSSFile {
    var fileName: String? = null
        private set
    private var zipFile: ZipOutputStream? = null
    private var zipFileStream: FileOutputStream? = null

    var code: String? = null
        set(value) {
            require(zipFile == null && zipFileStream == null) { "Code cannot be changed once the archive is opened." }
            require(this.code == null) { "Code can only be set once." }
            require(value != null && value != "") { "Code cannot be null or empty." }
            require(value.isAlNum()) { "Code can only contain alphanumeric characters, hyphen and underscore." }
            field = value.toLowerCase()
        }

    private val datasetReferenceStore: MutableList<DatasetReference> = mutableListOf()
    private var depiction: Any? = null
    private val ontologyStore: MutableList<OntologicalTerm> = mutableListOf()

    private val mutableNames: MutableSet<String> = mutableSetOf()

    val names: Set<String>
        get() { return setOf(*this.mutableNames.toTypedArray()) }

    var outputDirectory: Path = Path.of(".")
        set(value) {
            require(zipFile == null && zipFileStream == null) { "Directory cannot be changed once the archive is opened." }
            field = value
        }

    fun openArchive() {
        require(this.code != null)
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

    fun addEntry(path: String, content: String) {
        require(this.zipFile != null)
        val entry = ZipEntry(path)
        this.zipFile?.putNextEntry(entry)
        this.zipFile?.write(content.toByteArray())
    }

    fun addFile(pathInArchive: String, sourceFileName: String) {
        require(this.zipFile != null)
        val entry = ZipEntry(pathInArchive)
        this.zipFile?.putNextEntry(entry)
        val fileInputStream =
            FileInputStream(File(sourceFileName))
        val bytes = ByteArray(1024)
        var length: Int
        while (fileInputStream.read(bytes).also { length = it } >= 0) {
            this.zipFile?.write(bytes, 0, length)
        }
        fileInputStream.close()
    }

    fun addName(name: String) = this.mutableNames.add(name)
    fun addNames(names: List<String>) = names.map { this.addName(it) }

    fun setDepiction(depiction: Any) {
        this.depiction = depiction
    }
}