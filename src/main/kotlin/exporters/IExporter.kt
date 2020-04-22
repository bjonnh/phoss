package exporters

import java.io.InputStream

/**
 * Exporters are used to write the dataset… somewhere
 */
interface IExporter {
    /**
     * An exporter must be opened to allow writing
     */
    fun open(code: String)

    /**
     * Add an entry in the export with the given path and content stream.
     *
     * @param pathInArchive This is the relative path as will be written in the export archive
     * @param content an InputStream handling the content
     */
    fun addEntry(pathInArchive: String, content: InputStream)

    /**
     * Exporters may or may not write before a close, always close them.
     */
    fun close()
}