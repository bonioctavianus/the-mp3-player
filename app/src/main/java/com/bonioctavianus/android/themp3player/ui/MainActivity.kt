package com.bonioctavianus.android.themp3player.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bonioctavianus.android.themp3player.BuildConfig
import com.bonioctavianus.android.themp3player.R
import com.bonioctavianus.android.themp3player.utils.PermissionGranted
import com.bonioctavianus.android.themp3player.utils.isPermissionGranted
import org.greenrobot.eventbus.EventBus

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_PERMISSION_READ_STORAGE_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.title = ""
    }

    override fun onResume() {
        super.onResume()

        if (!isPermissionGranted()) {
            requestPermission()
        } else {
            EventBus.getDefault().post(
                PermissionGranted()
            )
        }
    }

    private fun requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            createRequestPermissionDialog()

        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_PERMISSION_READ_STORAGE_CODE
            )
        }
    }

    private fun createRequestPermissionDialog() {
        AlertDialog.Builder(this)
            .setCancelable(false)
            .setTitle(R.string.dialog_request_permission_title)
            .setMessage(R.string.dialog_request_permission_description)
            .setPositiveButton(R.string.dialog_request_permission_positive_action) { _, _ ->
                startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:${BuildConfig.APPLICATION_ID}")
                    )
                )
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                finish()
            }
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_PERMISSION_READ_STORAGE_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    EventBus.getDefault().post(
                        PermissionGranted()
                    )
                } else {
                    finish()
                }
            }
        }
    }
}
