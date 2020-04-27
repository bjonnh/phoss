package processors.spectroscopy

import dataset.PHOSSDataset
import dataset.Spectrum
import helpers.findFileMatch
import mu.KotlinLogging
import processors.ProcessingException
import processors.Processor
import processors.ProcessorStatus
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Path

class JeolProcessor(override val dataset: PHOSSDataset, private val directory: Path) : Processor<Spectrum> {
    override val logger = KotlinLogging.logger {}
    override val name: String = "JeolProcessor"
    override val help: String = "Process JEOL NMR datasets"
    override var status: ProcessorStatus = ProcessorStatus.FRESH

    private fun getStringFromChannel(channel: FileChannel, offset: Long, size: Int): String {
        val buffer = ByteBuffer.allocate(size)
        channel.read(buffer, offset)
        return String(buffer.array(), UTF_8)
    }

    private fun getByteFromChannel(channel: FileChannel, offset: Long): Byte {
        val buffer = ByteBuffer.allocate(1)
        channel.read(buffer, offset)
        return buffer.get(0)
    }

    private fun extractTitle(channel: FileChannel): String = getStringFromChannel(channel, 48, 124)
    private fun extractDimension(channel: FileChannel): NMRSpectrumType {
        return when(getByteFromChannel(channel, 12).toInt()) {
            1 -> NMRSpectrumType.S1D
            2 -> NMRSpectrumType.S2D
            else -> throw ProcessingException("unsupported spectral type")
        }
    }

    override fun process(function: (Spectrum) -> Unit) {
        directory.findFileMatch("glob:**/*.jdf").map { spectralPath ->
            logger.error("Found a file $spectralPath")
            try {
                logger.debug("Processing $spectralPath")
                val reader = RandomAccessFile(spectralPath.toFile(), "r")
                val channel = reader.channel

                val spectrum = Spectrum(this.extractTitle(channel),
                    "Test description",
                    this.extractDimension(channel).text, spectralPath.toString(), "")
                function(spectrum)
            } catch (e: ProcessingException) {
                dataset.statusUpdate(name, ProcessorStatus.WARNING, "${e.message} in directory $spectralPath")
            }
        }

        this.status = ProcessorStatus.SUCCESSFUL
    }
}
