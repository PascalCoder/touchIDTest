package com.thepascal.touchidtest

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.thepascal.login.biometrics.BiometricCallback
import com.thepascal.login.biometrics.BiometricManagerx
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity(), BiometricCallback {

    private var isPermissionGranted = false
    private lateinit var biometricManagerx: BiometricManagerx

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val promptInfo = androidx.biometric.BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.touchID_title))
            .setSubtitle(getString(R.string.touchID_subtitle))
            .setDescription(getString(R.string.touchID_description))
            .setNegativeButtonText(getString(R.string.touchID_negative_btn_text))
            .build()

        biometricManagerx = BiometricManagerx(this, this)

        if(isPermissionGranted){

        }

        if(cbUseBiometric.isChecked){
            isPermissionGranted = true
            Toast.makeText(this, "You've already granted that permission!", Toast.LENGTH_SHORT).show()
            PermissionHelper.requestBiometricPermission(this)
        }

        cbUseBiometric.setOnClickListener {
            if(cbUseBiometric.isChecked) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    Toast.makeText(this@LoginActivity, "Your device does not support biometrics", Toast.LENGTH_LONG)
                        .show()
                    return@setOnClickListener
                }

                biometricManagerx.checkRequirements(this)

                //biometricManagerx.biometricPrompt.authenticate(promptInfo)
            }
        }

        btnLogIn.setOnClickListener {
            /*if(ContextCompat.checkSelfPermission(this@LoginActivity,
                    Manifest.permission.USE_FINGERPRINT) ==
                    PackageManager.PERMISSION_GRANTED){
                isPermissionGranted = true
                Toast.makeText(this, "You've already granted that permission!", Toast.LENGTH_SHORT).show()

            }else{
                PermissionHelper.requestBiometricPermission(this)
            }*/
            if(cbUseBiometric.isChecked) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    Toast.makeText(this@LoginActivity, "Your device does not support biometrics", Toast.LENGTH_LONG)
                        .show()
                    return@setOnClickListener
                }

                biometricManagerx.checkRequirements(this)

                biometricManagerx.biometricPrompt.authenticate(promptInfo)
            }else{
                if(etEmail.text.toString().equals("pascal.arvee@gmail.com")
                    && etPasswordL.text.toString().equals("1234")){
                    onAuthenticationSuccessful()
                }else{
                    Toast.makeText(this@LoginActivity, "User not recognized", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == PermissionHelper.BIOMETRIC_PERMISSION_CODE){
            if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSdkVersionNotSupported() {
        Toast.makeText(applicationContext, getString(R.string.biometric_error_sdk_not_supported), Toast.LENGTH_LONG).show()
    }

    override fun onBiometricAuthenticationNotSupported() {
        Toast.makeText(applicationContext, getString(R.string.biometric_error_hardware_not_supported), Toast.LENGTH_SHORT   ).show()
    }

    override fun onBiometricAuthenticationNotAvailable() {
        Toast.makeText(applicationContext, getString(R.string.biometric_error_fingerprint_not_available), Toast.LENGTH_SHORT).show()
    }

    override fun onBiometricAuthenticationPermissionNotGranted() {
        Toast.makeText(applicationContext, getString(R.string.biometric_error_permission_not_granted), Toast.LENGTH_LONG).show()
    }

    override fun onBiometricAuthenticationInternalError(error: String) {
        Toast.makeText(applicationContext, error, Toast.LENGTH_LONG).show()
    }

    override fun onAuthenticationFailed() {
        this.runOnUiThread {
            Toast.makeText(applicationContext, getString(R.string.biometric_failure), Toast.LENGTH_LONG).show()
        }
    }

    override fun onAuthenticationCancelled() {
        this.runOnUiThread{
            Toast.makeText(applicationContext, getString(R.string.biometric_cancelled), Toast.LENGTH_LONG).show()
            //mBiometricManager.cancelAuthentication()
            biometricManagerx.cancelAuthentication()
        }
    }

    override fun onAuthenticationSuccessful() {
        this.runOnUiThread {
            Toast.makeText(applicationContext, getString(R.string.biometric_success), Toast.LENGTH_LONG).show()
            startActivity(Intent(this@LoginActivity, AccountActivity::class.java))
        }

        //will start a new activity here or should I use an interactor?
        //startActivity(Intent(this, AccountActivity::class.java))
    }

    override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence) {
        Toast.makeText(applicationContext, helpString, Toast.LENGTH_LONG).show()
    }

    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
        this.runOnUiThread {
            if(errString.toString().equals("CANCEL")){
                onAuthenticationCancelled()
            }
            //Toast.makeText(applicationContext, errString, Toast.LENGTH_LONG).show()
        }
    }
}
