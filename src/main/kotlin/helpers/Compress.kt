package helpers

import org.apache.commons.compress.parallel.InputStreamSupplier
import java.io.InputStream


fun createInputStreamSupplier(payload: InputStream): InputStreamSupplier? {
    return InputStreamSupplier { payload }
}