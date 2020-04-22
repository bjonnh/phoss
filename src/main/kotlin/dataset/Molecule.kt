package dataset

import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
data class Molecule(
    val names: List<String>,
    val inchi: String,
    val inchikey: String,
    val smiles: String,
    val path: String,
    val depiction: String
)


