package com.thepascal.touchidtest

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private var isPermissionGranted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if(isPermissionGranted){

        }

        btnLogIn.setOnClickListener {
            if(ContextCompat.checkSelfPermission(this@LoginActivity,
                    Manifest.permission.USE_FINGERPRINT) ==
                    PackageManager.PERMISSION_GRANTED){
                isPermissionGranted = true

            }else{
                requestBiometricPermission(this)
            }

        }
    }
}
