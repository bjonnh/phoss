package processors

import dataset.PHOSSDataset
import kotlinx.serialization.Serializable
import mu.KLogger
import org.apache.commons.lang3.time.StopWatch

@Serializable
enum class ProcessorStatus(val value: String) {
    SKIPPED("Skipped"),
    FAILED("Failed"),
    WARNING("Warning"),
    SUCCESSFUL("Successful"),
    FRESH("Fresh")
}

interface Processor<A>{
    val dataset: PHOSSDataset?
    val name: String
    val help: String
    var status: ProcessorStatus
    val logger: KLogger

    /**
     * Should not be called externally ideally as they would not register
     */
    fun process(function: (A) -> Unit) {}

    fun run(function: (A) -> Unit) {
        val stopWatch = StopWatch()
        stopWatch.start()
        try {
            process(function)
            stopWatch.stop()
            dataset?.statusUpdate(name, status, "Runtime ${stopWatch.time} ms")
        } catch (e: ProcessingException) {
            this.status = ProcessorStatus.FAILED
            dataset?.statusUpdate(name, ProcessorStatus.FAILED, e.message)
        }
    }
}

class ProcessingException(message: String): Exception(message)
class FatalProcessingException(message: String): Exception(message)

