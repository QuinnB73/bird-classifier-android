package honours.project.bird_classifier.tools

import android.content.Context
import android.content.res.Resources
import android.util.Log
import java.io.Serializable
import java.lang.Exception

/**
 * The Bird model. Implements the Serializable interface so that it can be easily passed between
 * activities.
 *
 * @param name The name of the bird
 */
data class Bird(val name: String): Serializable {
    companion object {
        private const val TAG = "BIRD"
    }

    var link = ""
    var photoCredit = ""
    var imageDrawableId: Int? = null
    val displayName: String = name.replace("_", " ")
        .split(" ").map { it.capitalize() }.joinToString(" ")

    /**
     * Setup the properties of the bird model. These require access to a ResourcesManager and
     * a Context.
     *
     * @param resources The ResourcesManager instance to use to load resources
     * @param context The Context to use to load the resources
     */
    fun setupBirdProperties(resources: Resources, context: Context) {
        try {
            imageDrawableId = resources.getIdentifier(name, "drawable", context.packageName)
            val linkId = resources.getIdentifier("${name}_link", "string", context.packageName)
            val photoCreditId = resources.getIdentifier("${name}_credit", "string", context.packageName)

            link = resources.getString(linkId)
            photoCredit = resources.getString(photoCreditId)
        } catch (error: Exception) {
            Log.e(TAG, "$error")
        }
    }
}
