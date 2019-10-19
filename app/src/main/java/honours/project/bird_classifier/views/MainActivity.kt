package honours.project.bird_classifier.views

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.hardware.camera2.CameraManager
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import honours.project.bird_classifier.R

import kotlinx.android.synthetic.main.activity_main.*
import net.steamcrafted.materialiconlib.MaterialDrawableBuilder

class MainActivity : AppCompatActivity() {

    private val PERMISSION_REQUEST_CODE = 0
    private val IMAGE_REQUEST_CODE = 1
    private val PERMISSION_MAP = mutableMapOf(android.Manifest.permission.CAMERA to false)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        setupCameraButton(applicationContext)

        checkPermissions()
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

    private fun setupCameraButton(context: Context) {
        val cameraButton: FloatingActionButton = findViewById(R.id.camera_button)
        val cameraIcon: Drawable = MaterialDrawableBuilder.with(context)
            .setIcon(MaterialDrawableBuilder.IconValue.CAMERA)
            .setColor(Color.WHITE)
            .setToActionbarSize()
            .build()

        cameraButton.setImageDrawable(cameraIcon)
        cameraButton.setOnClickListener(getCameraButtonListener())
    }

    private fun getCameraButtonListener(): (View) -> Unit {
        return {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
                intent.resolveActivity(packageManager)?.also {
                    Toast.makeText(applicationContext, "Take a picture", Toast.LENGTH_SHORT).also { toast ->
                        toast.show()
                    }
                    startActivityForResult(intent, IMAGE_REQUEST_CODE)
                }
            }
        }
    }

    private fun checkPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        PERMISSION_MAP.keys.forEach { permission ->
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission)
            } else {
                PERMISSION_MAP.put(permission, true)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissions(permissionsToRequest.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }

    private fun openCamera() {
    }
}
