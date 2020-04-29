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
import kotlin.math.roundToInt

typealias JeolChannel = FileChannel
typealias JeolOffset = Long
typealias JeolSize = Int

enum class JeolEndianness {
    BIG_ENDIAN,
    LITTLE_ENDIAN
}

fun JeolChannel.getStringFromChannel(offset: JeolOffset, size: JeolSize): String {
    val buffer = ByteBuffer.allocate(size)
    this.read(buffer, offset)
    return String(buffer.array(), UTF_8)
}

fun JeolChannel.getByteFromChannel(offset: JeolOffset): Byte {
    val buffer = ByteBuffer.allocate(1)
    this.read(buffer, offset)
    return buffer.get(0)
}

/**
 * Get a FloatArray from the channel
 *
 * Size is in bytes!
 */
fun JeolChannel.getDoubleArrayFromChannel(offset: JeolOffset, size: JeolSize): DoubleArray {
    val buffer = ByteBuffer.allocate(size)
    val fbuffer = buffer.asDoubleBuffer()
    this.read(buffer, offset)
    val farr = DoubleArray(fbuffer.remaining())
    fbuffer.get(farr)
    return farr
}

val JeolChannel.handled: Boolean
    get() {
        if (this.endianess == JeolEndianness.BIG_ENDIAN) return false
        return true
    }

val JeolChannel.endianess: JeolEndianness
    get() {
        return when (this.getByteFromChannel(8).toInt()) {
            0 -> JeolEndianness.BIG_ENDIAN
            1 -> JeolEndianness.LITTLE_ENDIAN
            else -> throw ProcessingException("unsupported endianess, this is likely a broken file")
        }
    }
val JeolChannel.title: String
    get() = this.getStringFromChannel(48, 124)

val JeolChannel.comment: String
    get() = this.getStringFromChannel(680, 128)

val JeolChannel.dimension: NMRSpectrumType
    get() {
        return when (this.getByteFromChannel(12).toInt()) {
            1 -> NMRSpectrumType.S1D
            2 -> NMRSpectrumType.S2D
            else -> throw ProcessingException("unsupported spectral type")
        }
    }

val JeolChannel.frequencies: List<Double>
    get() {
        return this.getDoubleArrayFromChannel(1064,64).toList()
    }

/**
 * A processor for JEOL files
 */
class JeolProcessor(override val dataset: PHOSSDataset, private val directory: Path) : Processor<Spectrum> {
    override val logger = KotlinLogging.logger {}
    override val name: String = "JeolProcessor"
    override val help: String = "Process JEOL NMR datasets"
    override var status: ProcessorStatus = ProcessorStatus.FRESH

    override fun process(function: (Spectrum) -> Unit) {
        directory.findFileMatch("glob:**/*.jdf").map { spectralPath ->
            try {
                val metadata = mutableMapOf<String, String>()
                logger.debug("Processing $spectralPath")
                val reader = RandomAccessFile(spectralPath.toFile(), "r")
                val channel = reader.channel as JeolChannel

                if (!channel.handled) throw ProcessingException("This file format is not handled")

                val dimension = channel.dimension
                val frequencies = channel.frequencies
                logger.debug("Frequencies: $frequencies")

                metadata["nmrType"] = dimension.text
                metadata["frequencies_raw"] = frequencies.joinToString(" ")
                metadata["frequencies"] = when (dimension) {
                    NMRSpectrumType.S1D -> "${frequencies[0].roundToInt()} Mhz"
                    NMRSpectrumType.S2D -> frequencies.subList(0, 2).joinToString(" ") { "${it.roundToInt()} MHz" }
                }

                val spectrum = Spectrum(
                    channel.title,
                    channel.comment,
                    "NMR",
                    spectralPath.toString(), "", metadata)

                function(spectrum)
            } catch (e: ProcessingException) {
                dataset.statusUpdate(name, ProcessorStatus.WARNING, "${e.message} in directory $spectralPath")
            }
        }

        this.status = ProcessorStatus.SUCCESSFUL
    }
}
