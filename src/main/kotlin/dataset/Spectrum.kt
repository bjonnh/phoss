package dataset

import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
data class Spectrum(
    val title: String,
    val description: String,
    val type: String,
    val path: String,
    val depiction: String?,
    val metadata: Map<String, String> = mapOf()
)
