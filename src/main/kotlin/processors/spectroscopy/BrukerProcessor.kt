package processors.spectroscopy

import dataset.PHOSSDataset
import dataset.Spectrum
import helpers.findFile
import helpers.recursiveApply
import mu.KotlinLogging
import processors.ProcessingException
import processors.Processor
import processors.ProcessorStatus
import java.nio.file.Path
import kotlin.math.roundToInt

data class AcqusMetaData(
    var frequencies: MutableList<Float> = mutableListOf(),
)

typealias FilesAdd=Pair<String, Path>

class BrukerProcessor(override val dataset: PHOSSDataset, private val directory: Path) : Processor<Spectrum> {
    override val logger = KotlinLogging.logger {}
    override val name: String = "BrukerProcessor"
    override val help: String = "Process Bruker NMR datasets"
    override var status: ProcessorStatus = ProcessorStatus.FRESH

    private val filteredFiles = listOf("1r", "1i", "2rr", "2ri", "2ir", "2ii")

    private val preAddFiles: MutableList<FilesAdd> = mutableListOf()

    private fun cleanName(name: String) = name.replace(" ", "_")

    private fun spectralType(path: Path): NMRSpectrumType {
        return when {
            (path.resolve("ser").toFile().exists() || path.resolve("pdata/2rr").toFile()
                .exists()) -> NMRSpectrumType.S2D
            (path.resolve("fid").toFile().exists() || path.resolve("pdata/1r").toFile().exists()
                    || path.resolve("pdata/1i").toFile().exists()) -> NMRSpectrumType.S1D
            else -> throw ProcessingException("unsupported spectral type")
        }
    }

    private fun acqusMetaData(path: Path): AcqusMetaData {
        val metadata = AcqusMetaData()
        val acqusFile = path.resolve("acqus").toFile()
        if (!acqusFile.exists()) throw ProcessingException("'acqus' file not found")
        acqusFile.readLines().map { line ->
            when {
                line.startsWith("##\$BF") -> metadata.frequencies.add(line.split(" ")[1].toFloat())
                else -> {}
            }
        }
        return metadata
    }

    private fun processDataset(spectralPath: Path): Spectrum {
        val relativePath = dataset.directory.relativize(spectralPath).toString()
        logger.debug("Found a bruker dataset: $relativePath")
        val description = spectralPath.resolve("pdata/1/title").toFile()
        if (!description.exists()) throw ProcessingException("no title file in the experiment")
        val descriptionText = description.readText()
        val title = descriptionText.lines().first().trim()

        spectralPath.recursiveApply {
            if (it.fileName.toString().toLowerCase() !in filteredFiles) {
                preAddFiles.add(Pair(dataset.relPath(Path.of(cleanName(it.toString()))), it))
            }
        }

        val metadata = mutableMapOf<String, String>()
        val spectralType = this.spectralType(spectralPath)
        metadata["nmrType"] = spectralType.text
        val frequencies = acqusMetaData(spectralPath).frequencies
        metadata["frequencies_raw"] = frequencies.joinToString(" ")
        metadata["frequencies"] = when (spectralType) {
            NMRSpectrumType.S1D -> "${frequencies[0].roundToInt()} Mhz"
            NMRSpectrumType.S2D -> frequencies.subList(0, 2).joinToString(" ") { "${it.roundToInt()} MHz" }
        }

        return Spectrum(
            title,
            descriptionText,
            "NMR",
            cleanName(relativePath),
            "",
            metadata
        )
    }

    override fun process(function: (Spectrum) -> Unit) {
        directory.findFile("audita.txt", true).map { it.parent }.map { spectralPath ->
            try {
                val spectrum = processDataset(spectralPath)
                preAddFiles.forEach { dataset.addFile(it.first, it.second) }
                function(spectrum)
                preAddFiles.clear()
            } catch (e: ProcessingException) {
                dataset.statusUpdate(name, ProcessorStatus.WARNING, "${e.message} in directory $spectralPath")
            }
        }

        this.status = ProcessorStatus.SUCCESSFUL
    }
}
