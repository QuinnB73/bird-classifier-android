package honours.project.bird_classifier.tools

import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.content.res.Resources
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import honours.project.bird_classifier.R
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*


@Throws(IOException::class)
fun createFile(): File? {
    val dirName = "bird_images"
    val customDir = File(Environment.getExternalStorageDirectory(), dirName)

    // Create the directory if it doesn't exist
    if (!customDir.exists()) {
        val success = customDir.mkdirs()
        if (!success) {
            return null
        }
    }

    // Build filename
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val filename = "JPEG_$timestamp"
    return File.createTempFile(filename, ".jpg", customDir)
}

fun updateGallery(imgUri: Uri, context: Context) {
    Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { intent ->
        intent.data = imgUri
        context.sendBroadcast(intent)
    }
}

