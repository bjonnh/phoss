import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.FileInputStream
import java.lang.IllegalArgumentException
import java.nio.file.Path
import java.util.zip.ZipInputStream

internal class BSSFileTest {

    @org.junit.jupiter.api.Test
    fun openArchive(@TempDir directory: Path) {
        val bss = BSSFile()
        bss.code = "TEST-ARCHIVE"
        bss.outputDirectory = directory
        bss.openArchive()
        bss.closeArchive()
        assertTrue(bss.fileName?.let { File(it).exists()} ?: false)
    }

    @org.junit.jupiter.api.Test
    fun `valid and invalid codes are handled`() {
        val bss = BSSFile()
        bss.code = "TESTtest-01"
        assertEquals("testtest-01", bss.code)

        val bssInvalid = BSSFile()
        assertThrows(
            IllegalArgumentException::class.java,
            { bssInvalid.code = "TESTtest-01." }
        )
    }

    @org.junit.jupiter.api.Test
    fun addDatasetReference() {
    }

    @org.junit.jupiter.api.Test
    fun addEntry(@TempDir directory: Path) {
        val bss = BSSFile()
        bss.code = "TEST-ARCHIVE"
        bss.outputDirectory = directory
        bss.openArchive()
        bss.addEntry("test.txt", "Test")
        bss.closeArchive()
        val zipInputStream = ZipInputStream(FileInputStream(bss.fileName))
        val entry = zipInputStream.nextEntry
        assertEquals("test.txt", entry.name)
        val content = zipInputStream.getTxtFile()
        assertEquals("Test", content.toString())
    }

    @org.junit.jupiter.api.Test
    fun addFile(@TempDir directory: Path) {
        val bss = BSSFile()
        bss.code = "TEST-ARCHIVE"
        bss.outputDirectory = directory
        bss.openArchive()
        val newFileName = "${directory.toAbsolutePath()}/test.txt"
        File(newFileName).printWriter(). use { out-> out.println("TestAddFile") }
        bss.addFile("test.txt", newFileName)
        bss.closeArchive()
        val zipInputStream = ZipInputStream(FileInputStream(bss.fileName))
        val entry = zipInputStream.nextEntry
        assertEquals("test.txt", entry.name)
        val content = zipInputStream.getTxtFile()
        assertEquals("TestAddFile", content.toString())
    }

    @org.junit.jupiter.api.Test
    fun addName() {

    }

    @org.junit.jupiter.api.Test
    fun addNames() {
        val bss = BSSFile()
        bss.addName("Test")
        bss.addNames(listOf("Test", "Too"))
        assertTrue(bss.names == setOf("Test", "Too"))
    }

    @org.junit.jupiter.api.Test
    fun setDepiction() {
    }
}