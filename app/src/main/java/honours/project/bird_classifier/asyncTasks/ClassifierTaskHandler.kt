package honours.project.bird_classifier.asyncTasks

import android.net.Uri

interface ClassifierTaskHandler {
    fun onComplete(identifiedBird: String, probability: Float, imgUri: Uri)
}