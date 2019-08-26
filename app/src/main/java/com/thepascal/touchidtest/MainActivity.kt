package com.thepascal.touchidtest

import android.content.Intent
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.thepascal.login.biometrics.BiometricCallback
import com.thepascal.login.biometrics.BiometricManager
import com.thepascal.login.biometrics.BiometricManagerx
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity(), BiometricCallback {

    private lateinit var mBiometricManager: BiometricManager
    private lateinit var biometricManagerx: BiometricManagerx
    var isBiometricEnabled:Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /* new dependency */

        val promptInfo = androidx.biometric.BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.touchID_title))
            .setSubtitle(getString(R.string.touchID_subtitle))
            .setDescription(getString(R.string.touchID_description))
            .setNegativeButtonText(getString(R.string.touchID_negative_btn_text))
            .build()

        biometricManagerx = BiometricManagerx(this, this)

        btnAuthenticate.setOnClickListener {
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
                Toast.makeText(this@MainActivity, "Your device does not support biometrics", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            biometricManagerx.checkRequirements(this)

            biometricManagerx.biometricPrompt.authenticate(promptInfo)
        }

        /*btnAuthenticate.setOnClickListener { //.BiometricBuilder(this@MainActivity)
            //Toast.makeText(applicationContext, "What's app!", Toast.LENGTH_SHORT).show()
            mBiometricManager = BiometricManager.BiometricBuilder(this@MainActivity)
                .setTitle(getString(R.string.touchID_title))
                .setSubtitle(getString(R.string.touchID_subtitle))
                .setDescription(getString(R.string.touchID_description))
                .setNegativeButtonText(getString(R.string.touchID_negative_btn_text))
                .build()

            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
                Toast.makeText(this@MainActivity, "Your device does not support biometrics", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.P){
                val biometricPrompt = androidx.biometric.BiometricPrompt(this, Executors.newSingleThreadExecutor(),
                    object: androidx.biometric.BiometricPrompt.AuthenticationCallback(){

                    })
            }

            mBiometricManager.authenticate(this@MainActivity)
        }*/
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
            startActivity(Intent(this@MainActivity, AccountActivity::class.java))
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
