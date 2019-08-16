package com.thepascal.touchidtest

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.thepascal.login.biometrics.BiometricCallback
import com.thepascal.login.biometrics.BiometricManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), BiometricCallback {

    private lateinit var mBiometricManager: BiometricManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnAuthenticate.setOnClickListener { //.BiometricBuilder(this@MainActivity)
            //Toast.makeText(applicationContext, "What's app!", Toast.LENGTH_SHORT).show()
            mBiometricManager = BiometricManager.BiometricBuilder(this@MainActivity)
                .setTitle(getString(R.string.touchID_title))
                .setSubtitle(getString(R.string.touchID_subtitle))
                .setDescription(getString(R.string.touchID_description))
                .setNegativeButtonText(getString(R.string.touchID_negative_btn_text))
                .build()

            if(mBiometricManager == null){

            }

            mBiometricManager.authenticate(this@MainActivity)
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
        Toast.makeText(applicationContext, getString(R.string.biometric_failure), Toast.LENGTH_LONG).show()
    }

    override fun onAuthenticationCancelled() {
        Toast.makeText(applicationContext, getString(R.string.biometric_cancelled), Toast.LENGTH_LONG).show()
        mBiometricManager.cancelAuthentication()
    }

    override fun onAuthenticationSuccessful() {
        Toast.makeText(applicationContext, getString(R.string.biometric_success), Toast.LENGTH_LONG).show()
        //will start a new activity here or should I use an interactor?
    }

    override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence) {
        Toast.makeText(applicationContext, helpString, Toast.LENGTH_LONG).show()
    }

    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
        Toast.makeText(applicationContext, errString, Toast.LENGTH_LONG).show()
    }

}
