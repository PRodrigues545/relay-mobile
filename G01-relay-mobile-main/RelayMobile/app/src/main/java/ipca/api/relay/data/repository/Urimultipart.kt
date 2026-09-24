package ipca.api.relay.data.repository

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

/**
 * Convert a content:// Uri into a Retrofit-ready multipart Part, streaming
 * from a temp file in [Context.cacheDir]. Streaming (rather than holding the
 * whole image in memory as a ByteArray) keeps large phone-camera photos from
 * pressuring the heap, and lets OkHttp re-read the body cleanly on retries
 * without touching the original content:// resolver — the temp file is the
 * authoritative source for the lifetime of the request.
 *
 * Android reclaims [Context.cacheDir] space under storage pressure, so no
 * manual cleanup is needed.
 *
 * @return null when the resolver cannot open the Uri (revoked permission,
 *         missing file, etc.) — callers should treat null as "skip the upload".
 */
internal fun uriToMultipart(
    context: Context,
    uri: Uri,
    partName: String = "file"
): MultipartBody.Part? {
    val resolver = context.contentResolver
    val mimeType = resolver.getType(uri) ?: "image/jpeg"
    val ext = mimeType.substringAfterLast('/', "jpg")

    val tempFile = File.createTempFile("upload_", ".$ext", context.cacheDir)
    val copied = resolver.openInputStream(uri)?.use { input ->
        tempFile.outputStream().use { output -> input.copyTo(output) }
        true
    } ?: false

    if (!copied) {
        tempFile.delete()
        return null
    }

    val body = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())
    return MultipartBody.Part.createFormData(partName, tempFile.name, body)
}