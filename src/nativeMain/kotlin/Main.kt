import okio.FileSystem
import okio.Path
import okio.buffer
import okio.use

fun main() {
    println("Hello, Kotlin/Native!")
    readlnOrNull()
}


/**
 * iterate full lines
 */
fun readLines(path: Path) {
    FileSystem.SYSTEM.source(path).use { fileSource ->
        fileSource.buffer().use { bufferedFileSource ->
            while (true) {
                val line = bufferedFileSource.readUtf8Line() ?: break
            }
        }
    }
}
