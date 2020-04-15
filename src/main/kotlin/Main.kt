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

fun main() {
    println("PHOSS")
    println("-----")

    PHOSSFinderProcessor(Path.of("data")).process {dataset ->
        dataset.openArchive()
        MoleculeProcessor(dataset, dataset.directory.resolve("molecules")).process {molecule ->
            dataset.addMolecule(molecule)
        }

        BrukerProcessor(dataset, dataset.directory.resolve("nmr")).process { spectrum ->
            dataset.addSpectrum(spectrum)
        }

        dataset.closeArchive()
    }
}