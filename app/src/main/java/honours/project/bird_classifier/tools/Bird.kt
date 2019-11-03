package honours.project.bird_classifier.tools

import android.content.Context
import android.content.res.Resources
import android.util.Log
import java.io.Serializable
import java.lang.Exception

data class Bird(val name: String): Serializable {
    companion object {
        private const val TAG = "BIRD"
    }

    var link = ""
    var photoCredit = ""
    var imageDrawableId: Int? = null
    val displayName: String = name.replace("_", " ")
        .split(" ").map { it.capitalize() }.joinToString(" ")

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
