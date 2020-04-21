package processors.chemistry

import dataset.Molecule
import dataset.PHOSSDataset
import helpers.findFile
import mu.KotlinLogging
import org.apache.commons.io.IOUtils
import org.openscience.cdk.AtomContainer
import org.openscience.cdk.depict.DepictionGenerator
import org.openscience.cdk.inchi.InChIGeneratorFactory
import org.openscience.cdk.io.MDLV2000Reader
import org.openscience.cdk.smiles.SmiFlavor
import org.openscience.cdk.smiles.SmilesGenerator
import processors.Processor
import processors.ProcessorStatus
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.file.Path
import javax.imageio.ImageIO

class MoleculeProcessor(private val dataset: PHOSSDataset, private val directory: Path) : Processor<Molecule> {
    override val logger = KotlinLogging.logger {}

    override val name: String = "MoleculeProcessor"

    override val help: String = "Convert the given molecule file to InChI, SMILES and depiction"

    override var status: ProcessorStatus = ProcessorStatus.FRESH

    private val inchiGeneratorFactory = InChIGeneratorFactory.getInstance()

    private val depictionGenerator = DepictionGenerator().withSize(256.0, 256.0).withAtomColors()

    private fun getNames(path: Path): List<String> {
        return path.findFile("names.txt", false).first().toFile().readLines().map { it.trim() }
    }

    override fun process(function: (Molecule) -> Unit) {
        directory.findFile("molecule.mol", true).map { moleculeMolPath ->
            val relativePath = dataset.directory.relativize(moleculeMolPath).toString()
            logger.debug("Found a molecule: $relativePath")
            val reader = MDLV2000Reader(moleculeMolPath.toFile().reader())
            val atomContainer = reader.read(AtomContainer())
            logger.debug("It has ${atomContainer.atomCount} atoms.")

            val inchiGenerator = inchiGeneratorFactory.getInChIGenerator(atomContainer, "")
            val smiles = SmilesGenerator(SmiFlavor.Absolute).create(atomContainer)


            val depiction = depictionGenerator.depict(atomContainer)
            dataset.addEntry("${moleculeMolPath.parent}/molecule.svg",
                depiction.toSvgStr())

            val stream: ByteArrayOutputStream = object : ByteArrayOutputStream() {
                @Synchronized
                override fun toByteArray(): ByteArray {
                    return buf
                }
            }

            ImageIO.write(depiction.toImg(), "png", stream)
            dataset.addEntry("${moleculeMolPath.parent}/molecule.png",
                ByteArrayInputStream(stream.toByteArray()))

            // Adding names

            moleculeMolPath.parent.findFile("names.txt", false).getOrNull(0)?.let {
                dataset.addFile("${moleculeMolPath.parent}/names.txt", it)
            }

            // Generating the serializable entry
            val molecule = Molecule(
                this.getNames(moleculeMolPath.parent),
                inchiGenerator.inchi,
                inchiGenerator.inchiKey,
                smiles,
                dataset.directory.relativize(moleculeMolPath.parent).toString(),
                dataset.directory.relativize(moleculeMolPath.parent).toString() + "/molecule.png"
            )

            function(molecule)
        }

        this.status = ProcessorStatus.SUCCESSFUL
    }
}