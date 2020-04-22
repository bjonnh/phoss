package processors

import dataset.Code
import dataset.PHOSSDataset
import helpers.findFile
import mu.KotlinLogging
import java.nio.file.Path

class PHOSSFinderProcessor(var directory: Path): Processor<PHOSSDataset> {
    override val logger = KotlinLogging.logger {}
    override val name = "PHOSSFinder"
    override val help = "Find any PHOSS dataset in the given directory"
    override var status: ProcessorStatus = ProcessorStatus.FRESH

    fun readCode(path: Path): Code {
        val newFileName = path.findFile("code.txt", false).first()
        return Code(newFileName.toFile().readText().trim())
    }

    /**
     * The function that localize datasets in the given directory
     */
    private fun findDatasets() = directory.findFile("code.txt", recursive=true).map { it.parent }

    override fun process(function: (PHOSSDataset) -> Unit) {
        this.findDatasets().map { path ->
            val code = this.readCode(path)
            logger.debug("Detected a new PHOSS dataset ${code}")
            function(PHOSSDataset(path, code))
        }
        this.status = ProcessorStatus.SUCCESSFUL
    }

    // As it is the first plugin of the serie and it is the one creating the dataset, it doesn't exist yet…
    override val dataset: PHOSSDataset? = null
}