package processors.report

import dataset.PHOSSDataset
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import mu.KotlinLogging
import processors.Processor
import processors.ProcessorStatus



class JSONReportProcessor(private val dataset: PHOSSDataset) : Processor<String> {
    override val logger = KotlinLogging.logger {}

    override val name: String = "JSONReportProcessor"

    override val help: String = "Generate a JSON report for this entry."

    override var status: ProcessorStatus = ProcessorStatus.FRESH

    override fun process(function: (String) -> Unit) {
        val report = Report(
            code = dataset.code.value,
            metadata = dataset.metadata,
            molecules = dataset.molecules,
            spectra = dataset.spectra
        )

        function(Json(JsonConfiguration.Stable).stringify(Report.serializer(), report))

        this.status = ProcessorStatus.SUCCESSFUL
    }
}


