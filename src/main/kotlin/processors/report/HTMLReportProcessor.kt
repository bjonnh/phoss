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
        context.put("version", "0.0.1-SNAPSHOT")
        context.put("date", SimpleDateFormat("yyyy-mm-dd hh:mm:ss").format(Calendar.getInstance().time))
        context.put("molecules", dataset.molecules)
        context.put("spectra", dataset.spectra)
        val writer = StringWriter()
        template.merge(context, writer)

        val generated = writer.buffer.toString() /*createHTMLDocument().html {
           head {
                title { +"PHOSS: ${dataset.code.value}" }
                link {
                    rel = "stylesheet"
                    href = "https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css"
                    integrity = "sha384-Vkoo8x4CGsO3+Hhxv8T/Q5PaXtkKtu6ug5TOeNV6gBiFeWPGFN9MuhOf23Q9Ifjh"
                    attributes["crossorigin"] = "anonymous"
                }
                style {
                    unsafe {+".content {padding: 3rem 1.5rem;}" }
                }
            }
            body {
              nav {
                    classes = setOf("navbar", "navbar-expand-md", "navbar-dark", "bg-dark", "fixed-top")
                    a("#") {
                        classes = setOf("navbar-brand")
                        +dataset.code.value
                    }
                }

             //   main {
                    //role = "main"
                    //classes = setOf("container")
               //     div {
                        //classes = setOf("content")
                 //       div(classes = "row") {
                            dataset.molecules.map { molecule ->
                      //          div(classes = "col-4") {
                        //            div {
                                        //classes = setOf("card", "molCard")
                          //              div(classes = "card-body") {
                                          /*  img(src = molecule.depiction,
                                                alt = "Molecule structure",
                                                classes = "card-img-top")
                                            h5 { +molecule.names.first() }*/
                                            table(classes = "table table-striped") {
                                                tbody {
                                                    /*molecule.names.subList(1, molecule.names.size).let { list ->
                                                        if (!list.isEmpty()) {
                                                            tr {
                                                                td { +"Other names" }
                                                                td { +(list.joinToString(" ") as String) }
                                                            }
                                                        }
                                                    }*/
                                                    this@tbody.tr {
                                                        this.td { +"InChI-Key" }
                                                    //    this.td { +(molecule.inchikey as String) }
                                                    }
                                                    this@tbody.tr {
                                                        this.td { +"InChI" }
                                                      //  this.td { +(molecule.inchi as String) }
                                                    }
                                                    this@tbody.tr {
                                                        this.td { +"Smiles" }
                                                        //this.td { +(molecule.smiles as String)}
                                                    }
                                                }
                                  //          }
                                //        }
                              //      }
                            //    }
                          //  }
                        //}
                    }
                }
/*
                script {
                    src = "https://code.jquery.com/jquery-3.4.1.slim.min.js"
                    integrity = "sha384-J6qa4849blE2+poT4WnyKhv5vZF5SrPo0iEjwBvKU7imGFAV0wwj1yYfoRSJoZ+n"
                    attributes["crossorigin"] = "anonymous"
                }
                script {
                    src = "https://cdn.jsdelivr.net/npm/popper.js@1.16.0/dist/umd/popper.min.js"
                    integrity = "sha384-Q6E9RHvbIyZFJoft+2mJbHaEWldlvI9IOYy5n3zV9zzTtmI3UksdQRVvoxMfooAo"
                    attributes["crossorigin"] = "anonymous"
                }
                script {
                    src = "https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/js/bootstrap.min.js"
                    integrity = "sha384-wfSDF2E50Y2D1uUdj0O3uMBJnjuUD4Ih7YwaYd1iqfktj0Uod8GCExl3Og8ifwB6"
                    attributes["crossorigin"] = "anonymous"
                }*/
            }

        }.toString()*/
        println(generated)
        function(generated)

        this.status = ProcessorStatus.SUCCESSFUL
    }
}


