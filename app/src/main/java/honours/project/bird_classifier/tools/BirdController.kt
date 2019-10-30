package honours.project.bird_classifier.tools

import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import honours.project.bird_classifier.R
import honours.project.bird_classifier.asyncTasks.ClassifierTaskHandler
import honours.project.bird_classifier.views.BirdActivity
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class BirdController(assetManager: AssetManager, resources: Resources) {
    companion object {
        private const val TAG = "BIRD_CONTROLLER"
        const val BIRD_EXTRA = "BIRD_EXTRA"
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
            birdMap[birdName] = bird
        }
    }

    fun setupBirdList(listView: ListView, context: Context) {
        val birdListAdapter: BirdArrayAdapter =
            BirdArrayAdapter(context, R.layout.bird_list_item, birdMap.values.toMutableList())

        listView.adapter = birdListAdapter
        listView.setOnItemClickListener { _, _, position, _ ->
            val bird = birdListAdapter.getItem(position)
            bird?.let {
                startActivityForBird(it.name, context)
            }
        }
    }

    fun classifyImage(img: Bitmap, handler: ClassifierTaskHandler) {
        birdClassifier.classifyImage(img, handler)
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

    fun startActivityForBird(name: String, context: Context) {
        val bird = birdMap[name]

        if (bird == null) {
            Log.e(TAG, "Invalid bird name $name")
            return
        }

        val intent = Intent(context, BirdActivity::class.java).apply {
            putExtra(BIRD_EXTRA, bird)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
