import exporters.DirExporter
import exporters.ZipExporter
import mu.KotlinLogging
import processors.PHOSSFinderProcessor
import processors.ProcessorStatus
import processors.chemistry.MoleculeProcessor
import processors.metadata.PHOSSMetadataProcessor
import processors.report.JSONReportProcessor
import processors.spectroscopy.BrukerProcessor
import processors.spectroscopy.JeolProcessor
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.file.Path

fun String.isAlNum(): Boolean {
    return this.matches(Regex("[A-Za-z0-9_-]+"))
}

fun InputStream.getTxtFile(): StringBuilder? {
    val out = StringBuilder()
    val reader = BufferedReader(InputStreamReader(this))
    var line: String?
    while (reader.readLine().also { line = it } != null) {
        out.append(line)
    }
    return out
}
val logger = KotlinLogging.logger {}
fun main() {
    logger.info("PHOSS")
    logger.info("-----")

    PHOSSFinderProcessor(Path.of("data").toAbsolutePath()).process {dataset ->
        //dataset.exporter = ZipExporter(Path.of("."), fastMode = true)
        dataset.exporter = DirExporter(Path.of("output"))
        dataset.open()
        logger.info("Starting the metadata processor")
        PHOSSMetadataProcessor(dataset, dataset.directory).run {}

        logger.info("Starting the molecule processor")
        MoleculeProcessor(dataset, dataset.directory.resolve("molecules")).run {molecule ->
            dataset.addMolecule(molecule)
        }
        logger.info("Starting the Bruker processor")
        BrukerProcessor(dataset, dataset.directory.resolve("nmr")).run { spectrum ->
            dataset.addSpectrum(spectrum)
        }
        logger.info("Starting the Jeol processor")
        JeolProcessor(dataset, dataset.directory.resolve("nmr")).run { spectrum ->
            dataset.addSpectrum(spectrum)
        }
        logger.info("Generating HTML report")
        HTMLReportProcessor(dataset).run { report ->
            dataset.addEntry("index.html", report)
            dataset.addEntry("script.js", "\$(document).ready(function () { \$('.ui.accordion').accordion();});")
        }
        logger.info("Generating JSON report")
        JSONReportProcessor(dataset).run { report ->
            dataset.addEntry("index.json", report)
        }
        logger.info("Closing archive")
        dataset.statusUpdate("PHOSSFinder", ProcessorStatus.SUCCESSFUL)
        dataset.close()
    }
    logger.info("Done")
}