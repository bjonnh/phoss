package processors.spectroscopy

import dataset.PHOSSDataset
import dataset.Spectrum
import helpers.findFile
import helpers.recursiveApply
import mu.KotlinLogging
import processors.Processor
import processors.ProcessorStatus
import java.nio.file.Path

/*

    def process_bruker(self, path):
        title = "Untitled (you should give a meaningful title to your NMR experiments)"
        print("  - Adding the experiment (filtering processed data out)")
        for file in glob.glob(f"{path}/**", recursive=True):
            file_name = os.path.basename(file)
            if file_name == "title":
                with open(file, 'r') as f:
                    title = f.read().strip()
            if file_name not in self.filteredFiles:
                new_name = self.relPath(file).replace(" ", "_")
                self.bssFile.add_file(file, new_name)
        self.datasets += [{"title": title, "path": self.relPath(path).replace(" ", "_"), "type": "bruker"}]

    def _process(self):
        for dataset in self.find_bruker():
            print(f"[NMRProcessor] found a Bruker dataset {self.relPath(dataset)}")
            self.process_bruker(dataset)
        report = ""
        for dataset in self.datasets:
            self.bssFile.add_dataset_reference(dataset)
            report += f"* {dataset['path']} :{dataset['type']}:\n"
            report += "\n".join(["    "+line for line in dataset['title'].split('\n')])
            report += "\n"
        self.bssFile.add_entry("nmr_experiments.txt", report)
        return ProcessorReturn.SUCCESSFUL

 */


 */


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
