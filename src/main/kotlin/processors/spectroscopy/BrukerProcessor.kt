package processors.spectroscopy

import dataset.PHOSSDataset
import dataset.Spectrum
import helpers.findFile
import helpers.recursiveApply
import mu.KotlinLogging
import processors.Processor
import processors.ProcessorStatus
import java.nio.file.Path


class BrukerProcessor(private val dataset: PHOSSDataset, private val directory: Path) : Processor<Spectrum> {
    override val logger = KotlinLogging.logger {}

    override val name: String = "BrukerProcessor"

    override val help: String = "Process Bruker NMR datasets"

    override var status: ProcessorStatus = ProcessorStatus.FRESH

    val filteredFiles = listOf("1r", "1i", "2rr", "2ri", "2ir", "2ii")

    fun cleanName(name: String) = name.replace(" ", "_")

    override fun process(function: (Spectrum) -> Unit) {
        directory.findFile("audita.txt", true).map { it.parent }.map { spectralPath ->
            val relativePath = dataset.directory.relativize(spectralPath).toString()
            logger.debug("Found a bruker dataset: $relativePath")
            val description = spectralPath.resolve("pdata/1/title").toFile().readText()
            val title = description.lines().first().trim()

            spectralPath.recursiveApply {
                if (it.fileName.toString().toLowerCase() !in filteredFiles) {
                    dataset.addFile(cleanName(it.toString()), it)
                }
            }

            val spectrum = Spectrum(
                title,
                description,
                "NMR",
                cleanName(relativePath),
                ""
            )
            function(spectrum)
        }

        this.status = ProcessorStatus.SUCCESSFUL
    }
}
