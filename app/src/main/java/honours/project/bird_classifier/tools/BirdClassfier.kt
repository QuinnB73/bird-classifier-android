package honours.project.bird_classifier.tools

import android.content.res.AssetManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.util.Log
import honours.project.bird_classifier.R
import org.tensorflow.lite.Interpreter
import java.io.*
import java.lang.Exception
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class BirdClassfier(private val assetManager: AssetManager, private val resources: Resources) {
    private val TAG = "BIRD_CLASSIFIER"
    private var interpreter: Interpreter? = null
    private val labels: MutableList<String> = mutableListOf()
    private var pixels: ByteBuffer? = null
    private var categoryProbabilities: Array<FloatArray>? = null

    init {
        val labelsFile = resources.getString(R.string.labels_path)
        val modelFile = resources.getString(R.string.model_path)
        val imgSize = resources.getInteger(R.integer.input_size)
        val batchSize = resources.getInteger(R.integer.batch_size)

        loadLabels(labelsFile)
        Log.i(TAG, "Successfully loaded $labelsFile")

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

        // Initialize ByteBuffer
        pixels = ByteBuffer.allocateDirect(batchSize * imgSize * imgSize * 3 * 4)
        pixels!!.order(ByteOrder.nativeOrder())
    }

    private fun loadLabels(labelsFile: String) {
        if (labels.isNotEmpty()) {
            // Log that labels are already loaded
            return
        }

        var bufferedReader: BufferedReader? = null

        try {
            bufferedReader = BufferedReader(
                InputStreamReader(
                    assetManager.open(labelsFile, AssetManager.ACCESS_BUFFER)
                )
            )

            while (true) {
                val line = bufferedReader.readLine() ?: break
                labels.add(line)
            }
        } catch (e: IOException) {
            // Log it
        } finally {
            bufferedReader?.close()
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

    // From https://www.tensorflow.org/lite/models/image_classification/android
    fun classifyImage(img: Bitmap) {
        pixels?.rewind()
        val size = img.height * img.width
        val intPixels: IntArray = IntArray(size)
        img.getPixels(intPixels, 0, img.width, 0, 0, img.width, img.height)

        for (pixel in intPixels) {
            addPixelValue(pixel)
        }

        categoryProbabilities = Array(1){ FloatArray(labels.size) }
        interpreter?.let {
            it.run(pixels, categoryProbabilities)

            categoryProbabilities?.get(0)?.forEachIndexed { index, value ->
                Log.e("CLASSIFYING", "Category: $index prob: $value, BIRD: ${labels[index]}")
            }
        }
    }

    private fun addPixelValue(pixel: Int) {
        val redValue = ((pixel shr 16) and 0xFF) / 255.0f
        val greenValue = ((pixel shr 8) and 0xFF) / 255.0f
        val blueValue = (pixel and 0xFF) / 255.0f

        pixels?.putFloat(redValue) // get R
        pixels?.putFloat(greenValue) // get G
        pixels?.putFloat(blueValue) // get B
    }
}
