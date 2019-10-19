package honours.project.bird_classifier.views

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.hardware.camera2.CameraManager
import honours.project.bird_classifier.R

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val PERMISSION_REQUEST_CODE = 0
    private val PERMISSION_MAP = mutableMapOf(android.Manifest.permission.CAMERA to false)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

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
                for (i in 0..grantResults.size - 1) {
                    if(grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        PERMISSION_MAP[permissions[i]] = true
                    }
                }
            }
            else -> {}
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
