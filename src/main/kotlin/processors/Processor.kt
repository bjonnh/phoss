package processors

import dataset.PHOSSDataset
import kotlinx.serialization.Serializable
import mu.KLogger

@Serializable
enum class ProcessorStatus(val value: String) {
    SKIPPED("Skipped"),
    FAILED("Failed"),
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
        dataset?.register(name)
        process(function)
        dataset?.statusUpdate(name, status)
    }
}

