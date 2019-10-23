package honours.project.bird_classifier.views

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.FileProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageActivity
import com.theartofdev.edmodo.cropper.CropImageView
import honours.project.bird_classifier.tools.*
import honours.project.bird_classifier.R

import kotlinx.android.synthetic.main.activity_main.*
import net.steamcrafted.materialiconlib.MaterialDrawableBuilder
import java.io.File
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private val PERMISSION_REQUEST_CODE = 0
    private val TAKE_IMG_REQUEST_CODE = 1
    private val LOAD_IMG_REQUEST_CODE = 2
    private val PERMISSION_MAP = mutableMapOf(
        android.Manifest.permission.CAMERA to false,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE to false,
        android.Manifest.permission.READ_EXTERNAL_STORAGE to false
    )

    private var currentImgUri: Uri? = null
    private var birdClassifier: BirdClassfier? = null

    companion object {
        private val CURRENT_IMG_URI = "CURRENT_IMG_URI"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        birdClassifier = BirdClassfier(assets, resources)

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
            currentImgUri = Uri.parse(getString(CURRENT_IMG_URI))
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
            R.id.action_settings -> true
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
                        startCropActivity(imgUri)
                    }
                }
            }
            LOAD_IMG_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(applicationContext, "Loaded image", Toast.LENGTH_LONG).show()
                    val imgUri: Uri? = data?.data
                    imgUri?.let { imgUri ->
                        startCropActivity(imgUri)
                    }
                }
            }
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result: CropImage.ActivityResult = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK) {
                    val uri = result.uri
                    var bitmap: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)

                    // resize bitmap to the required input size for the CNN
                    val targetSize = resources.getInteger(R.integer.input_size)
                    bitmap = Bitmap.createScaledBitmap(bitmap, targetSize, targetSize, true)

                    birdClassifier?.classifyImage(bitmap)
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    val error: Exception = result.error
                    print(error)
                }
            }
            else -> {}
        }
    }

    private fun startCropActivity(imgUri: Uri) =
        CropImage.activity(imgUri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .start(this)

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

    private fun getCameraButtonListener(): (View) -> Unit {
        return {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
                intent.resolveActivity(packageManager)?.also {
                    val file: File? = createFile()
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
