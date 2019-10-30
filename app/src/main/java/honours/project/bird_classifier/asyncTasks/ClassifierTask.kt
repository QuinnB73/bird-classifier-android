package honours.project.bird_classifier.asyncTasks

import android.graphics.Bitmap
import android.os.AsyncTask
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ClassifierTask(val interpreter: Interpreter, val labels: List<String>,
                     imgSize: Int, batchSize: Int, val handler: ClassifierTaskHandler):
        AsyncTask<Bitmap, Int, String?>() {

    companion object {
        private const val TAG = "CLASSIFIER_ASYNC_TASK"
    }

    private val pixels: ByteBuffer?

    init {
        // Initialize ByteBuffer
        pixels = ByteBuffer.allocateDirect(batchSize * imgSize * imgSize * 3 * 4)
        pixels!!.order(ByteOrder.nativeOrder())
    }

    override fun doInBackground(vararg images: Bitmap?): String? {
        if (images.size != 1) {
            Log.e(TAG, "Invalid argument passed to async classification task")
            return null
        }

        val bitmap = images[0]
        if (bitmap != null && pixels != null) {
            loadImageIntoPixelByteBuffer(bitmap)
        } else {
            return null
        }

        val categoryProbabilities = Array(1){ FloatArray(labels.size) }
        var identifiedBird = "Not found"

        interpreter.run(pixels, categoryProbabilities)

        categoryProbabilities[0].let { probabilities ->
            var maxIndex = -1
            var maxValue = -1.0f

            probabilities.forEachIndexed { i, probability ->
                Log.d(TAG, "Category: $i prob: $probability, BIRD: ${labels[i]}")

                if (probability > maxValue) {
                    maxIndex = i
                    maxValue = probability
                }
            }

            identifiedBird = if (maxIndex > -1) labels[maxIndex] else identifiedBird
            Log.d(TAG, "Identified bird: $identifiedBird")
        }
        return identifiedBird
    }

    override fun onPostExecute(result: String?) {
        val resultToPass: String = result ?: "Not found"
        Log.d(TAG, "Passing result to handler $resultToPass")

        handler.onComplete(resultToPass)
    }

    private fun loadImageIntoPixelByteBuffer(img: Bitmap) {
        pixels?.rewind()
        val size = img.height * img.width
        val intPixels = IntArray(size)
        img.getPixels(intPixels, 0, img.width, 0, 0, img.width, img.height)

        for (pixel in intPixels) {
            val redValue = ((pixel shr 16) and 0xFF) / 255.0f
            val greenValue = ((pixel shr 8) and 0xFF) / 255.0f
            val blueValue = (pixel and 0xFF) / 255.0f

            pixels?.putFloat(redValue)
            pixels?.putFloat(greenValue)
            pixels?.putFloat(blueValue)
        }
    }
}