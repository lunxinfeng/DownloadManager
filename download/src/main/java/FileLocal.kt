import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.BufferedSource
import okio.appendingSink
import okio.buffer
import java.io.File

object FileLocal {
    suspend fun write(
        source: BufferedSource,
        filePath: String
    ) {
        withContext(Dispatchers.IO) {
            val file = File(filePath)
            val bufferedSink = file.appendingSink().buffer()
            bufferedSink.writeAll(source)

            bufferedSink.close()
            source.close()
        }
    }
}