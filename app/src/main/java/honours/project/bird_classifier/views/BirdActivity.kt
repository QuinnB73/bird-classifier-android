package honours.project.bird_classifier.views

import android.os.Bundle
import android.service.autofill.TextValueSanitizer
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import honours.project.bird_classifier.R
import honours.project.bird_classifier.tools.Bird
import honours.project.bird_classifier.tools.BirdController

class BirdActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bird)

        val bird: Bird = intent.getSerializableExtra(BirdController.BIRD_EXTRA) as Bird
        bird.setupBirdProperties(resources, applicationContext)

        setupViews(bird)
    }

    private fun setupViews(bird: Bird) {
        val name: TextView = findViewById(R.id.bird_name)
        val photoCredit: TextView = findViewById(R.id.photo_credit)
        val link: TextView = findViewById(R.id.bird_description)

        name.append(bird.displayName)
        photoCredit.append(bird.photoCredit)
        link.append(bird.link)
    }
}