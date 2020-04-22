package dataset

import exporters.IExporter
import getDate
import mu.KotlinLogging
import org.apache.commons.io.IOUtils
import java.io.FileInputStream
import java.io.InputStream
import java.nio.file.Path
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import processors.ProcessorStatus
import java.text.SimpleDateFormat
import java.util.*


data class DatasetEntries(
    val molecules: MutableList<Molecule> = mutableListOf(),
    val spectra: MutableList<Spectrum> = mutableListOf(),
)

@Serializable
data class PHOSSDatasetMetadata(
    val synonyms: List<String> = listOf(),
    val creators: List<String> = listOf(),
)

@Serializable
data class ProcessorStatusDetail(
    val name: String,
    val status: ProcessorStatus,
    val timestamp: String
)

data class PHOSSDataset(
    val directory: Path,
    val code: Code,
) {
    private val datasetEntries = DatasetEntries()
    val logger = KotlinLogging.logger {}

    var exporter: IExporter? = null

    var metadata: PHOSSDatasetMetadata? = null

    private val statusStore: MutableList<ProcessorStatusDetail> = mutableListOf()

    val processorsStatus: List<ProcessorStatusDetail>
        get() {
            return statusStore.toList()
        }

    var fileName: String? = null
        private set

    private val mutableNames: MutableSet<String> = mutableSetOf()

    val names: Set<String>
        get() {
            return setOf(*this.mutableNames.toTypedArray())
        }

    val molecules get() = this.datasetEntries.molecules
    val spectra get() = this.datasetEntries.spectra


    fun addEntry(pathInArchive: String, content: InputStream) {
        require(this.exporter != null) { "This dataset has no exporter." }
        this.exporter?.addEntry(pathInArchive, content)
    }

    fun addEntry(pathInArchive: String, content: String) {
        this.addEntry(pathInArchive, IOUtils.toInputStream(content))
    }

    fun relPath(path: Path) = this.directory.relativize(path).toString()

    fun addFile(pathInArchive: String, sourcePath: Path) {
        this.addEntry(pathInArchive,
            FileInputStream(sourcePath.toFile()))
    }

    fun addName(name: String) = this.mutableNames.add(name)
    fun addNames(names: List<String>) = names.map { this.addName(it) }

    fun addMolecule(molecule: Molecule) = this.datasetEntries.molecules.add(molecule)
    fun addSpectrum(spectrum: Spectrum) = this.datasetEntries.spectra.add(spectrum)

    /**
     * Open the exporter
     */
    fun open() {
        require(this.exporter != null) { "Cannot open a non existing exporter." }
        this.exporter?.open(this.code.value)
    }

    /**
     * Close the exporter
     */
    fun close() {
        this.exporter?.close()
    }

    fun register(name: String) {
        this.statusUpdate(name, ProcessorStatus.FRESH)
    }

    fun statusUpdate(name: String, status: ProcessorStatus) {
        statusStore.add(
            ProcessorStatusDetail(
                name,
                status,
                getDate()
            )
        )
    }
}