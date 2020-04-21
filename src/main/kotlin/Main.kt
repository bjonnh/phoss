import mu.KotlinLogging
import processors.PHOSSFinderProcessor
import processors.chemistry.MoleculeProcessor
import processors.spectroscopy.BrukerProcessor
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
        dataset.openArchive(fastMode=true)
        logger.info("Starting the molecule processor")
        MoleculeProcessor(dataset, dataset.directory.resolve("molecules")).process {molecule ->
            dataset.addMolecule(molecule)
        }
        logger.info("Starting the Bruker processor")
        BrukerProcessor(dataset, dataset.directory.resolve("nmr")).process { spectrum ->
            dataset.addSpectrum(spectrum)
        }
        logger.info("Closing archive")
        dataset.closeArchive()
    }
    logger.info("Done")
}