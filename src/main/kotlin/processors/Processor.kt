package processors

import mu.KLogger
import java.util.logging.Logger

enum class ProcessorStatus {
    SKIPPED,
    FAILED,
    SUCCESSFUL,
    FRESH
}

interface Processor<A>{
    val name: String
    val help: String
    var status: ProcessorStatus
    val logger: KLogger
    fun process(function: (A) -> Unit)
}

