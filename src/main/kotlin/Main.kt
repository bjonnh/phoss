import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader

fun String.isAlNum(): Boolean {
    return this.matches(Regex("[A-Za-z0-9_-]+"))
}

fun InputStream.getTxtFile(): StringBuilder? {
    val out = StringBuilder()
    val reader = BufferedReader(InputStreamReader(this))
    var line: String?
    while (reader.readLine().also { line = it } != null) {
        out.append(line)
    }
    return out
}

fun main() {
    println("PHOSS")
    println("-----")

    File("./data").list()?.map { println(it) }
}