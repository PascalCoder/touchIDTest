package com.thepascal.touchidtest

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat

const val BIOMETRIC_PERMISSION_CODE = 1

fun requestBiometricPermission(activity: Activity){
    if(ActivityCompat.shouldShowRequestPermissionRationale(activity,
            Manifest.permission.USE_BIOMETRIC)){
        AlertDialog.Builder(activity)
            .setTitle("Permission needed")
            .setMessage("The app needs your permission to use biometric authentication")
            .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.USE_FINGERPRINT),
                    BIOMETRIC_PERMISSION_CODE)
            })
            .setNegativeButton("CANCEL") {dialog, which ->
                dialog.dismiss()
            }
            .create()
            .show()
    }else{
        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.USE_FINGERPRINT),
            BIOMETRIC_PERMISSION_CODE)
    }
}