package processors.report

import dataset.Molecule
import dataset.PHOSSDatasetMetadata
import dataset.Spectrum
import kotlinx.serialization.Serializable

@Serializable
data class Report(
    val code: String,
    val metadata: PHOSSDatasetMetadata?,
    val molecules: List<Molecule>,
    val spectra: List<Spectrum>
)