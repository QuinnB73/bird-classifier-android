package honours.project.bird_classifier.asyncTasks

import android.net.Uri

/**
 * Interface for classes to implement to act as handlers for the completion of a ClassifierTask that
 * they register with.
 */
interface ClassifierTaskHandler {

    /**
     * This function will be called on completion of the ClassifierTask
     *
     * @param identifiedBird The bird that was identified
     * @param probability The confidence that the identifiedBird is correct
     * @param imgUri The URI of the image that was classified
     */
    fun onComplete(identifiedBird: String, probability: Float, imgUri: Uri)
}