package honours.project.bird_classifier.tools

import android.content.ContentResolver
import android.content.res.AssetManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import honours.project.bird_classifier.R
import honours.project.bird_classifier.asyncTasks.ClassifierTask
import honours.project.bird_classifier.asyncTasks.ClassifierTaskHandler
import org.tensorflow.lite.Interpreter
import java.io.*
import java.lang.Exception
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

// Adapted from https://www.tensorflow.org/lite/models/image_classification/android
class BirdClassifier(private val assetManager: AssetManager, private val resources: Resources,
                     private val labels: List<String>) {
    companion object {
        private const val TAG = "BIRD_CLASSIFIER"
    }

    private var interpreter: Interpreter? = null

    init {
        val modelFile = resources.getString(R.string.model_path)

        // Load the interpreter
        try {
            val options = Interpreter.Options().apply {
                setUseNNAPI(true)
            }
            interpreter = Interpreter(loadModelFile(modelFile), options)
            Log.i(TAG, "Successfully loaded $modelFile")
        } catch (e: Exception) {
            Log.e(TAG, e.localizedMessage)
        }
    }

    private fun loadModelFile(modelFile: String): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd(modelFile)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel

        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset,
            fileDescriptor.declaredLength
        )
    }

    fun classifyImage(imgUri: Uri, contentResolver: ContentResolver, handler: ClassifierTaskHandler) {
        interpreter?.let {
            val imgSize = resources.getInteger(R.integer.input_size)
            val batchSize = resources.getInteger(R.integer.batch_size)
            var bitmap: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imgUri)

            // resize bitmap to the required input size for the CNN
            bitmap = Bitmap.createScaledBitmap(bitmap, imgSize, imgSize, true)

            val classifierTask = ClassifierTask(it, labels, imgSize, batchSize, handler, imgUri)
            classifierTask.execute(bitmap)
        }
    }
}
