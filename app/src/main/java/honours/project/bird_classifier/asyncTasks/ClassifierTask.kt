package honours.project.bird_classifier.asyncTasks

import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * This class is an AsyncTask for classifying images using the provided CNN and the TensorFlow Lite
 * library in the background.
 *
 * @constructor Initializes a ByteBuffer for the image pixel data
 *
 * @param interpreter The TensorFlow Lite interpreter to use to classify the image data
 * @param labels The labels that correspond with the output
 * @param imgSize The image input size of the CNN
 * @param batchSize The batch size to use. Should be 1.
 * @param handler The object to pass the result to on completion
 * @param imgUri The URI of the image being classified
 */
class ClassifierTask(private val interpreter: Interpreter,
                     private val labels: List<String>,
                     imgSize: Int, batchSize: Int,
                     private val handler: ClassifierTaskHandler,
                     private val imgUri: Uri):
        AsyncTask<Bitmap, Int, ClassifierTask.ClassifierResult?>() {

    companion object {
        private const val TAG = "CLASSIFIER_ASYNC_TASK"
    }

    /**
     * Inner class to wrap the result of the classification into an object
     *
     * @param identifiedBird The bird that was identified
     * @param probability The output of the CNN layer that corresponds with the bird
     */
    class ClassifierResult(val identifiedBird: String?, val probability: Float?)

    private val pixels: ByteBuffer?

    init {
        // Initialize ByteBuffer
        pixels = ByteBuffer.allocateDirect(batchSize * imgSize * imgSize * 3 * 4)
        pixels!!.order(ByteOrder.nativeOrder())
    }

    override fun doInBackground(vararg images: Bitmap?): ClassifierResult? {
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
        var probability = 0.0f

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
            probability = if (maxValue > -1) maxValue else probability
            Log.d(TAG, "Identified bird: $identifiedBird")
        }
        return ClassifierResult(identifiedBird, probability)
    }

    override fun onPostExecute(result: ClassifierResult?) {
        Log.d(TAG, "Result to pass to handler $result")
        result?.let {
            if (it.identifiedBird != null && it.probability != null) {
                handler.onComplete(it.identifiedBird, it.probability, imgUri)
            }
        }
    }

    /**
     * Load Bitmap data into a ByteBuffer storing the pixel data. This was adapted from the code in
     * this example from TensorFlow:
     * https://github.com/tensorflow/examples/tree/master/lite/examples/image_classification/android
     *
     * @param img The Bitmap data to load into the ByteBuffer
     */
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