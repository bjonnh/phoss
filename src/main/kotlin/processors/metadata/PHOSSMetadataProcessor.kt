package processors.metadata

import dataset.PHOSSDataset
import dataset.PHOSSDatasetMetadata
import helpers.findFile
import mu.KotlinLogging
import processors.Processor
import processors.ProcessorStatus
import java.nio.file.Path

class PHOSSMetadataProcessor(private val dataset: PHOSSDataset, private val directory: Path) : Processor<String> {
    override val logger = KotlinLogging.logger {}
    override val name: String = "PHOSSMetadataProcessor"
    override val help: String = "Process PHOSS basic metadata entries"
    override var status: ProcessorStatus = ProcessorStatus.FRESH

    override fun process(function: (String) -> Unit) {
        val synonyms = directory.findFile("synonyms.txt", recursive=false).flatMap { path ->
            path.toFile().readText().lines().map { it.trim() }.filter { it!=""}
        }
        val creators = directory.findFile("creators.txt", recursive=false).flatMap {path ->
            path.toFile().readText().lines().map { it.trim() }.filter { it!=""}
        }
        dataset.metadata = PHOSSDatasetMetadata(synonyms, creators)
    }
}