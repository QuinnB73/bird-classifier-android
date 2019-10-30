package honours.project.bird_classifier.tools

import android.content.Context
import android.content.res.Resources
import java.io.Serializable

data class Bird(val name: String): Serializable {
    var link = ""
    var photoCredit = ""
    var photoFileName = "${name}.jpg"
    val displayName: String = name.replace("_", " ")
        .split(" ").map { it.capitalize() }.joinToString(" ")

    fun setupBirdProperties(resources: Resources, context: Context) {
        val linkId = resources.getIdentifier("${name}_link", "string", context.packageName)
        val photoCreditId = resources.getIdentifier("${name}_credit", "string", context.packageName)

        link = resources.getString(linkId)
        photoCredit = resources.getString(photoCreditId)
    }
}
