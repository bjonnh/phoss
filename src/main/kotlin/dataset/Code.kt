package dataset

import isAlNum

class Code(value: String) {
    var value: String

    init {
        require(value != "") { "dataset.Code cannot be empty." }
        require(value.isAlNum()) { "dataset.Code can only contain alphanumeric characters, hyphen and underscore." }
        this.value = value.toLowerCase()
    }

    override fun toString(): String = this.value
}