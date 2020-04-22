package processors.spectroscopy

import dataset.PHOSSDataset
import dataset.Spectrum
import helpers.findFile
import helpers.recursiveApply
import mu.KotlinLogging
import processors.Processor
import processors.ProcessorStatus
import java.nio.file.Path

enum class NMRSpectrumType(val text: String) {
    S1D("1D"),
    S2D("2D")
}

data class AcqusMetaData(
    var frequencies: MutableList<Float> = mutableListOf(),
)


class BrukerProcessor(private val dataset: PHOSSDataset, private val directory: Path) : Processor<Spectrum> {
    override val logger = KotlinLogging.logger {}
    override val name: String = "BrukerProcessor"
    override val help: String = "Process Bruker NMR datasets"
    override var status: ProcessorStatus = ProcessorStatus.FRESH

    val filteredFiles = listOf("1r", "1i", "2rr", "2ri", "2ir", "2ii")


    fun cleanName(name: String) = name.replace(" ", "_")

    fun spectralType(path: Path): NMRSpectrumType {
        return when {
            (path.resolve("ser").toFile().exists() || path.resolve("pdata/2rr").toFile()
                .exists()) -> NMRSpectrumType.S2D
            (path.resolve("fid").toFile().exists() || path.resolve("pdata/1r").toFile().exists()
                    || path.resolve("pdata/1i").toFile().exists()) -> NMRSpectrumType.S1D
            else -> throw Exception("Unknown spectral type")
        }
    }

    fun acqusMetaData(path: Path): AcqusMetaData {
        val metadata = AcqusMetaData()
        println(path.resolve("acqus"))
        path.resolve("acqus").toFile().readLines().map { line ->
            println(line)
            when {
                line.startsWith("##\$BF") -> metadata.frequencies.add(line.split(" ")[1].toFloat())
                else -> {
                }
            }
        }
        return metadata
    }

    override fun process(function: (Spectrum) -> Unit) {
        directory.findFile("audita.txt", true).map { it.parent }.map { spectralPath ->
            val relativePath = dataset.directory.relativize(spectralPath).toString()
            logger.debug("Found a bruker dataset: $relativePath")
            val description = spectralPath.resolve("pdata/1/title").toFile().readText()
            val title = description.lines().first().trim()

            spectralPath.recursiveApply {
                if (it.fileName.toString().toLowerCase() !in filteredFiles) {
                    dataset.addFile(dataset.relPath(Path.of(cleanName(it.toString()))), it)
                }
            }

            val metadata = mutableMapOf<String, String>()
            val spectralType = this.spectralType(spectralPath)
            metadata["nmrType"] = spectralType.text
            val frequencies = acqusMetaData(spectralPath).frequencies
            metadata["frequencies"] = when (spectralType) {
                NMRSpectrumType.S1D -> "${frequencies[0]} Mhz"
                NMRSpectrumType.S2D -> frequencies.subList(0, 2).map { "$it MHz" }.joinToString(" ")
            }

            val spectrum = Spectrum(
                title,
                description,
                "NMR",
                cleanName(relativePath),
                "",
                metadata
            )
            function(spectrum)
        }

        this.status = ProcessorStatus.SUCCESSFUL
    }
}
