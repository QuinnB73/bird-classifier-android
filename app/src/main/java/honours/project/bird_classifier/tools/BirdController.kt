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

    fun classifyImage(imgUri: Uri, contentResolver: ContentResolver, handler: ClassifierTaskHandler) {
        birdClassifier.classifyImage(imgUri, contentResolver, handler)
    }

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
