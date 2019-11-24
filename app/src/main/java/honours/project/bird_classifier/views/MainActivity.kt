package honours.project.bird_classifier.views

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.content.FileProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import honours.project.bird_classifier.tools.*
import honours.project.bird_classifier.R
import honours.project.bird_classifier.asyncTasks.ClassifierTaskHandler

import kotlinx.android.synthetic.main.activity_main.*
import net.steamcrafted.materialiconlib.MaterialDrawableBuilder
import java.io.File
import java.lang.Exception

/**
 * The main activity
 */
class MainActivity : AppCompatActivity(), ClassifierTaskHandler {

    companion object {
        private const val TAG = "MAIN_ACTIVITY"
        private const val PERMISSION_REQUEST_CODE = 0
        private const val TAKE_IMG_REQUEST_CODE = 1
        private const val LOAD_IMG_REQUEST_CODE = 2
        private const val CURRENT_IMG_URI = "CURRENT_IMG_URI"
    }

    private val PERMISSION_MAP = mutableMapOf(
        android.Manifest.permission.CAMERA to false,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE to false,
        android.Manifest.permission.READ_EXTERNAL_STORAGE to false
    )

    private var currentImgUri: Uri? = null
    private var birdController: BirdController? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        val listView: ListView = findViewById(R.id.birds_list)

        birdController = BirdController(assets, resources, applicationContext)
        birdController?.setupBirdList(listView, applicationContext)

        setupFloatingButtons(applicationContext)

        checkPermissions()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.run {
            currentImgUri?.let {
                putString(CURRENT_IMG_URI, currentImgUri.toString())
            }
        }

        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        savedInstanceState?.run {
            try {
                currentImgUri = Uri.parse(getString(CURRENT_IMG_URI))
            } catch (error: Exception) {
                Log.e(TAG, error.localizedMessage)
            }
        }

        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_help -> handleHelpClicked()
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        when(requestCode) {
            PERMISSION_REQUEST_CODE -> {
                grantResults.forEachIndexed { index, result ->
                    if (result == PackageManager.PERMISSION_GRANTED) {
                        PERMISSION_MAP[permissions[index]] = true
                    } else {
                        // Close the application because it cannot function without all of its
                        // permissions
                        finishAndRemoveTask()
                    }
                }
            }
            else -> {}
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode) {
            TAKE_IMG_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(applicationContext, "Success", Toast.LENGTH_LONG).show()
                    currentImgUri?.let { imgUri ->
                        updateGallery(imgUri, applicationContext)
                        startCropActivity(imgUri)
                    }
                }
            }
            LOAD_IMG_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(applicationContext, "Loaded image", Toast.LENGTH_LONG).show()
                    val imgUri: Uri? = data?.data
                    imgUri?.let { it ->
                        startCropActivity(it)
                    }
                }
            }
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result: CropImage.ActivityResult = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK) {
                    val uri = result.uri

                    birdController?.classifyImage(uri, contentResolver, this)
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    val error: Exception = result.error
                    Log.e(TAG, error.localizedMessage)
                }
            }
            else -> {}
        }
    }

    override fun onComplete(identifiedBird: String, probability: Float, imgUri: Uri) {
        Toast.makeText(applicationContext, "Found bird: $identifiedBird", Toast.LENGTH_LONG).show()
        birdController?.startActivityForBird(identifiedBird, probability, imgUri, applicationContext)
    }

    /**
     * Handles the about menu item being clicked by popping up a window with information about
     * the app.
     *
     * @return true
     */
    private fun handleHelpClicked(): Boolean {
        val rootGroup = findViewById<ViewGroup>(android.R.id.content)
        val viewRoot = rootGroup.rootView as View

        val layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = layoutInflater.inflate(R.layout.about, rootGroup, false)
        val wh = LinearLayout.LayoutParams.WRAP_CONTENT
        val popupWindow = PopupWindow(popupView, wh, wh, true)

        val elevation = resources.getString(R.string.popup_elev).toFloat()
        popupWindow.elevation = elevation

        popupWindow.showAtLocation(viewRoot, Gravity.CENTER, 0, 0)

        return true
    }

    /**
     * Start the CropImageActivity from the Android Image Cropper library
     *
     * @param imgUri The URI of the image to crop
     */
    private fun startCropActivity(imgUri: Uri) =
        CropImage.activity(imgUri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .start(this)

    /**
     * Set up the two floating buttons; the camera button and the load image button.
     *
     * @param context The Context to use when setting up drawable icons
     */
    private fun setupFloatingButtons(context: Context) {
        val cameraButton: FloatingActionButton = findViewById(R.id.camera_button)
        val cameraIcon: Drawable = MaterialDrawableBuilder.with(context)
            .setIcon(MaterialDrawableBuilder.IconValue.CAMERA)
            .setColor(Color.WHITE)
            .setToActionbarSize()
            .build()
        val loadButton: FloatingActionButton = findViewById(R.id.load_button)
        val galleryIcon: Drawable = MaterialDrawableBuilder.with(context)
            .setIcon(MaterialDrawableBuilder.IconValue.CAMERA_IMAGE)
            .setColor(Color.WHITE)
            .setToActionbarSize()
            .build()

        cameraButton.setImageDrawable(cameraIcon)
        cameraButton.setOnClickListener(getCameraButtonListener())

        loadButton.setImageDrawable(galleryIcon)
        loadButton.setOnClickListener(getGalleryLoadButtonListener())
    }

    /**
     * Get a listener for the camera FAB
     *
     * @return A function that takes in a View as a parameter and does not return anything
     */
    private fun getCameraButtonListener(): (View) -> Unit {
        return {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
                intent.resolveActivity(packageManager)?.also {
                    val file: File? = createFile(resources)
                    file?.let {
                        currentImgUri = FileProvider.getUriForFile(
                            this,
                            "honours.project.bird_classifier.fileprovider",
                            it)
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, currentImgUri)
                        startActivityForResult(intent, TAKE_IMG_REQUEST_CODE)
                    }
                }
            }
        }
    }

    /**
     * Get a listener for the image load button FAB
     *
     * @return A function that takes in a View as a parameter and does not return anything
     */
    private fun getGalleryLoadButtonListener(): (View) -> Unit {
        return {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }
            intent.resolveActivity(packageManager)?.let {
                startActivityForResult(intent, LOAD_IMG_REQUEST_CODE)
            }
        }
    }

    /**
     * Check the permissions of the application
     */
    private fun checkPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        PERMISSION_MAP.keys.forEach { permission ->
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission)
            } else {
                PERMISSION_MAP[permission] = true
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissions(permissionsToRequest.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }
}
