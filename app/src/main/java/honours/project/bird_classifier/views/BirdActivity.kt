package honours.project.bird_classifier.views

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.service.autofill.TextValueSanitizer
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import honours.project.bird_classifier.R
import honours.project.bird_classifier.tools.Bird
import honours.project.bird_classifier.tools.BirdController
import kotlinx.android.synthetic.main.activity_bird.*
import java.lang.Exception

class BirdActivity: AppCompatActivity() {

    companion object {
        private const val TAG = "BIRD_ARCTIVITY"
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

    private fun setupViews(bird: Bird, probability: Float, imgUri: Uri?) {
        val image: ImageView = findViewById(R.id.bird_image)
        val confidence: TextView = findViewById(R.id.confidence)
        val photoCredit: TextView = findViewById(R.id.photo_credit)
        val link: TextView = findViewById(R.id.bird_description)

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

        val confidenceString: String = if (probability > 0) probability.toString() else "N/A"
        confidence.append(confidenceString)

        link.append(bird.link)
    }
}