package honours.project.bird_classifier.asyncTasks

interface ClassifierTaskHandler {
    fun onComplete(identifiedBird: String)
}