import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okio.BufferedSource
import java.lang.Exception
import java.net.UnknownHostException
import java.time.Duration

object Http {
    private val client = OkHttpClient.Builder()
        .connectTimeout(Duration.ofSeconds(60))
        .build()

    suspend fun getLength(
        url: String
    ): Long? {
        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
            val call = client.newCall(request)
            val response = call.execute()
            response.close()
            response.body?.contentLength()
        }
    }

    suspend fun download(
        url: String,
        range: String
    ): BufferedSource? {
        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(url)
                .addHeader("Range", range)
                .get()
                .build()
            val call = client.newCall(request)
            val response = call.execute()
            response.body?.source()
        }
    }
}