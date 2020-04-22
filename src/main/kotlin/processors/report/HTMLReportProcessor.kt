import dataset.PHOSSDataset
import mu.KotlinLogging
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.RuntimeConstants
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader
import processors.Processor
import processors.ProcessorStatus
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*


class HTMLReportProcessor(private val dataset: PHOSSDataset) : Processor<String> {
    override val logger = KotlinLogging.logger {}

    override val name: String = "HTMLReportProcessor"

    override val help: String = "Generate an HTML report for this entry."

    override var status: ProcessorStatus = ProcessorStatus.FRESH

    override fun process(function: (String) -> Unit) {
        val engine = VelocityEngine()

        engine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath")
        engine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader::class.java.name)
        engine.init()
        val template = engine.getTemplate("index.vm")
        val context = VelocityContext()
        context.put("code", dataset.code.value)
        context.put("metadata", dataset.metadata)
        context.put("version", "0.0.1-SNAPSHOT")
        context.put("date", SimpleDateFormat("yyyy-mm-dd hh:mm:ss").format(Calendar.getInstance().time))
        context.put("molecules", dataset.molecules)
        context.put("spectra", dataset.spectra)
        val writer = StringWriter()
        template.merge(context, writer)

        val generated = writer.buffer.toString()
        function(generated)

        this.status = ProcessorStatus.SUCCESSFUL
    }
}


