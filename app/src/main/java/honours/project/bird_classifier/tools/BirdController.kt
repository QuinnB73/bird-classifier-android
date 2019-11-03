package honours.project.bird_classifier.tools

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import honours.project.bird_classifier.R
import honours.project.bird_classifier.asyncTasks.ClassifierTaskHandler
import honours.project.bird_classifier.views.BirdActivity
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * The logic controller of the application
 *
 * @param assetManager The AssetManager to give to the BirdClassifier to load the TensorFlow Lite
 *                     model and to use to load the categories file provided in the assets folder
 * @param resources The ResourceManager to use to setup Bird objects, load categories, and give to
 *                  the BirdClassifier object
 * @param context The Context to give to Bird objects when initializing them
 *
 * @constructor Loads all categories from the assets file, creates a BirdClassifier object for
 *              for later classification, and initializes Bird objects for every bird found in the
 *              categories
 */
class BirdController(assetManager: AssetManager, private val resources: Resources,
                     private val context: Context) {
    companion object {
        private const val TAG = "BIRD_CONTROLLER"
        const val BIRD_EXTRA = "BIRD_EXTRA"
        const val PROB_EXTRA = "PROB_EXTRA"
        const val IMG_URI_EXTRA = "IMG_EXTRA"
    }
    private val birdMap: MutableMap<String, Bird> = mutableMapOf()
    private val labels: List<String> = loadCategories(assetManager, resources)
    private val birdClassifier: BirdClassifier = BirdClassifier(assetManager, resources, labels)

    init {
        initializeBirds()
    }

    private fun initializeBirds() {
        Log.i(TAG, "Initializing bird data")

        labels.forEach { birdName ->
            val bird = Bird(birdName)
            bird.setupBirdProperties(resources, context)
            birdMap[birdName] = bird
        }
    }

    /**
     * Sets the adapter for the ListView passed in. Also sets the onClickListener for each list
     * item.
     *
     * @param listView The ListView to set the adapter for
     * @param context The Context to use to start a new activity when a list item is clicked
     */
    fun setupBirdList(listView: ListView, context: Context) {
        val birdList: MutableList<Bird> = birdMap.values.sortedBy{ it.name }.toMutableList()
        val birdListAdapter = BirdArrayAdapter(context, R.layout.bird_list_item, birdList)

        listView.adapter = birdListAdapter
        listView.setOnItemClickListener { _, _, position, _ ->
            val bird = birdListAdapter.getItem(position)
            bird?.let {
                startActivityForBird(it.name, -1.0f, null, context)
            }
        }
    }

    /**
     * Classify an image using the BirdClassifier object
     *
     * @param imgUri The URI of the image to classify
     * @param contentResolver The ContentResolver to use to read the image
     * @param handler The object to notify of the completion of the classification
     */
    fun classifyImage(imgUri: Uri, contentResolver: ContentResolver, handler: ClassifierTaskHandler) {
        birdClassifier.classifyImage(imgUri, contentResolver, handler)
    }

    /**
     * Load the categories in the assets file into a list of strings
     *
     * @param assetManager The AssetManager to use to load the categories
     * @param resources The ResourcesManager to use to get the path to the categories file
     *
     * @return A list of strings containing the categories found in the file
     */
    private fun loadCategories(assetManager: AssetManager, resources: Resources): List<String> {
        val labels: MutableList<String> = mutableListOf()
        val labelsFileFullPath = resources.getString(R.string.labels_path)
        var bufferedReader: BufferedReader? = null

        try {
            bufferedReader = BufferedReader(
                InputStreamReader(
                    assetManager.open(labelsFileFullPath, AssetManager.ACCESS_BUFFER)
                )
            )

            while (true) {
                val line = bufferedReader.readLine() ?: break
                labels.add(line)
            }
            Log.i(TAG, "Successfully loaded ${R.string.labels_path}")
        } catch (e: IOException) {
            Log.e(TAG, "Unable to load categories: ${e.localizedMessage}")
        } finally {
            bufferedReader?.close()
            return labels
        }
    }

    /**
     * Start an activity for the bird identified by the passed in name.
     *
     * @param name The name of the bird that identifies which Bird object to give to the activity
     * @param probability The CNN's confidence that the identified bird was correct. Only
     *                    applicable if the activity is started due to a classification. If started
     *                    via a click on a ListView item, probability should be < 0.
     * @param imgUri The URI of the image to display in the activity. If one is not provided, (i.e
     *               null is passed in), a default image will be used.
     * @param context The Context used to start the activity
     */
    fun startActivityForBird(name: String, probability: Float, imgUri: Uri?, context: Context) {
        val bird = birdMap[name]

        if (bird == null) {
            Log.e(TAG, "Invalid bird name $name")
            return
        }

        val intent = Intent(context, BirdActivity::class.java).apply {
            putExtra(BIRD_EXTRA, bird)
            putExtra(PROB_EXTRA, probability)
            imgUri?.let {
                putExtra(IMG_URI_EXTRA, it.toString())
            }
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
