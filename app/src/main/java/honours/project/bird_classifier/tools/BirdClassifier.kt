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

/**
 * Sets up the TensorFlow Lite Interpreter for the TensorFlow Lite model.
 *
 * @param assetManager The AssetManager used to load the model in the assets directory
 * @param resources The ResourcesManager used to load resources
 * @param labels The categories to classify images into
 *
 * @constructor Loads the model file and sets up a TensorFlow Lite Interpreter.
 */
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

    /**
     * Load the model file from the assets
     *
     * @param modelFile The path to the model in the assets folder
     *
     * @return A MappedByteBuffer of the entire file, read only
     */
    private fun loadModelFile(modelFile: String): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd(modelFile)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel

        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset,
            fileDescriptor.declaredLength
        )
    }

    /**
     * Classify the image provided, have the result be passed to the handler provided. This process
     * is asynchronous using the ClassifierTask.
     *
     * @param imgUri The URI of the image to be classified
     * @param contentResolver The ContentResolver to use to load the image into a Bitmap
     * @param handler The handler to notify on completion
     */
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
