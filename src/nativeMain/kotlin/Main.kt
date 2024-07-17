import com.soywiz.krypto.sha256
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.pointed
import kotlinx.cinterop.toKString
import okio.*
import okio.Path.Companion.toPath
import platform.posix.closedir
import platform.posix.exit
import platform.posix.opendir
import platform.posix.readdir
import kotlin.random.Random


val r = Random.Default

fun main() {
    print("path: ")
    val pathStr = readlnOrNull() ?: throw RuntimeException("no path")
    val path = pathStr.toPath().normalized()
    require(FileSystem.SYSTEM.exists(path)) { ex(0, "path not exist") }
    println("path: $path")
    println("are you sure to use this dir? all files in this dir might be deleted. (y/n)")
    val line = readlnOrNull()
    if (line == null || !line.contains("y")) {
        ex(0)
    }

    for (i in 0..100) {
        try {
            writeRand(path)
        } catch (e: Exception) {
            ex(0, e.message)
        }
    }

    while (true) {
        try {
            writeRand(path)
            val randFile = iterateDir(path).random()
            readFull(randFile)
            deleteFile(randFile)
        } catch (e: Exception) {
            ex(0, e.message)
        }
    }
}


fun ex(int: Int, msg: String? = null) {
    println("exit: $int $msg")
    readlnOrNull()
    exit(int)
}

/**
 * iterate full lines
 */
@Throws(IOException::class)
fun readFull(path: Path) {
    println("read from $path")
    val bytes = FileSystem.SYSTEM.source(path).buffer().use { bufferedSource ->
        bufferedSource.readByteArray()
    }

    val hashHex = bytes.sha256().hex
    val fileName = path.name

    require(hashHex == fileName) { "$hashHex != $fileName" }
}


@Throws(IOException::class)
fun writeRand(parentDir: Path) {
    var bytes: ByteArray
    var hashHex: String
    do {
        bytes = r.nextBytes(r.nextInt(1000, 5_000_000)) // 1kb - 5mb
        hashHex = bytes.sha256().hex
    } while (FileSystem.SYSTEM.exists(parentDir.resolve(hashHex)))

    println("write to $parentDir")
    require(parentDir.parent != null) { "path parent is null" }
    FileSystem.SYSTEM.write(parentDir.resolve(hashHex)) {
        write(bytes)
    }
}


@Throws(IOException::class)
fun deleteFile(path: Path) {
    println("delete $path")
    FileSystem.SYSTEM.delete(path, mustExist = false)
}

@OptIn(ExperimentalForeignApi::class)
fun iterateDir(path: Path): List<Path> {
    println("iterate $path")
    val ret = mutableListOf<Path>()
    val dir = opendir(path.toString()) ?: return emptyList()
    try {
        var entryPoint = readdir(dir)
        while (entryPoint != null) {
            val fileName = entryPoint.pointed.d_name.toKString()
            if (fileName !in listOf(".", "..")) {
                ret.add(path.resolve(fileName))
            }
            entryPoint = readdir(dir)
        }
    } finally {
        closedir(dir)
    }

    return ret
}
