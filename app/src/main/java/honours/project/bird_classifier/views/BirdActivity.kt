package honours.project.bird_classifier.views

import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import honours.project.bird_classifier.R
import honours.project.bird_classifier.tools.Bird
import honours.project.bird_classifier.tools.BirdController
import kotlinx.android.synthetic.main.activity_bird.*
import java.lang.Exception

/**
 * The Activity to display Bird information
 */
class BirdActivity: AppCompatActivity() {

    companion object {
        private const val TAG = "BIRD_ACTIVITY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bird)
        setSupportActionBar(toolbar)

        val bird: Bird = intent.getSerializableExtra(BirdController.BIRD_EXTRA) as Bird
        val probability: Float = intent.getFloatExtra(BirdController.PROB_EXTRA, -1.0f)
        val imgUriStr: String? = intent.getStringExtra(BirdController.IMG_URI_EXTRA)

        var imgUri: Uri? = null
        imgUriStr?.let {
            imgUri = Uri.parse(it)
        }

        supportActionBar?.title = bird.displayName
        setupViews(bird, probability, imgUri)
    }

    /**
     * Setup the views in the activity with the information from the bird
     *
     * @param bird The Bird to use to fill information
     * @param probability The confidence score from the CNN. Only applicable if an image was
     *                    classified. If this activity did not start due to a classification,
     *                    probability is expected to be < 0.
     * @param imgUri The URI of the image to load into the main ImageView of the activity. If null,
     *               the default image ID stored in the bird object will be used.
     */
    private fun setupViews(bird: Bird, probability: Float, imgUri: Uri?) {
        val image: ImageView = findViewById(R.id.bird_image)
        val confidence: TextView = findViewById(R.id.confidence)
        val photoCredit: TextView = findViewById(R.id.photo_credit)
        val link: TextView = findViewById(R.id.bird_description)

        setupImageAndCredit(bird, image, imgUri, photoCredit)
        setupConfidenceText(probability, confidence)
        setupLink(bird, link)
    }

    /**
     * Setup the ImageView with the image and the photo credit
     *
     * @param bird The Bird object to use to get information
     * @param image The ImageView to update
     * @param imgUri The URI of the image to load into the ImageView. If null, the default image ID
     *               from the Bird object will be loaded.
     * @param photoCredit The TextView to populate with the credit for the photo, if using the
     *                    default one provided by the app.
     */
    private fun setupImageAndCredit(bird: Bird, image: ImageView, imgUri: Uri?, photoCredit: TextView) {
        try {
            if (imgUri != null) {
                image.setImageURI(imgUri)
                photoCredit.append("Loaded from this phone")
            } else if (bird.imageDrawableId != null) {
                image.setImageResource(bird.imageDrawableId!!)
                photoCredit.append(bird.photoCredit)
            }
            image.contentDescription = bird.displayName

        } catch (error: Exception) {
            Log.e(TAG, "Unable to load image: $error")
        }
    }

    /**
     * Setup the TextView indicating the confidence score of the CNN result. If this activity was
     * started due to an image classification, this will be the confidence rounder to the second
     * decimal place. If started due to a click on the ListView item, it will be "N/A"
     *
     * @param probability The confidence score of the CNN result. Should be < 0 if the activity was
     *                    not started due to a classification
     * @param confidence The TextView to populate with the string
     */
    private fun setupConfidenceText(probability: Float, confidence: TextView) {
        var confidenceString = "N/A"
        if (probability > 0) {
            confidenceString = "%.2f".format((probability * 100)) + "%"
        }
        confidence.append(confidenceString)
    }

    /**
     * Setup the TextView that will contain a link where users can get more information about the
     * bird.
     *
     * @param bird The Bird object to get the link from
     * @param link The TextView to populate with the link
     */
    private fun setupLink(bird: Bird, link: TextView) {
        val linkStr = "<a href=\"${bird.link}\">${bird.displayName}</a>"
        link.isClickable = true
        link.movementMethod = LinkMovementMethod.getInstance()
        link.append(Html.fromHtml(linkStr, Html.FROM_HTML_MODE_LEGACY))
    }
}