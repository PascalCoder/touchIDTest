package com.thepascal.login.biometrics

import android.content.Intent
import android.os.Build
import android.os.Handler
import android.provider.Settings
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.core.os.CancellationSignal
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executors

class BiometricManagerx(var activity: FragmentActivity, var biometricCallback: BiometricCallback) {

    private val mCancellationSignal = CancellationSignal()

    var biometricPrompt = BiometricPrompt(activity, Executors.newSingleThreadExecutor(), object: BiometricPrompt.AuthenticationCallback(){
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                biometricCallback.onAuthenticationError(errorCode, errString)
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                biometricCallback.onAuthenticationSuccessful()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                biometricCallback.onAuthenticationFailed()
            }
        })


    fun cancelAuthentication(){
        if(BiometricUtils.isBiometricEnabled()){
            if(!mCancellationSignal.isCanceled) mCancellationSignal.cancel()
        }
    }

    fun checkRequirements(biometricCallback: BiometricCallback) {
        if (!BiometricUtils.isSdkVersionSupported()){
            biometricCallback.onSdkVersionNotSupported()
        }

        if(!BiometricUtils.isPermissionGranted(activity)){
            biometricCallback.onBiometricAuthenticationPermissionNotGranted()
        }

        if(!BiometricUtils.isHardwareSupported(activity)){
            biometricCallback.onBiometricAuthenticationNotSupported()
        }

        if(!BiometricUtils.isFingerprintAvailable(activity)){
            biometricCallback.onBiometricAuthenticationNotAvailable()

            promptUserToRegisterFingerprint()
        }
    }

    private fun promptUserToRegisterFingerprint() {
        //prompt the user to register a fingerprint for versions >= 28
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Toast.makeText(activity, "Please enroll a fingerprint", Toast.LENGTH_LONG).show()

            //Let's give time to Toast to disappear before sending the user to fingerprint
            // registration page
            Handler().postDelayed(
                {
                    //Implicit intent sending user to register a fingerprint for versions >= 28
                    activity.startActivity(Intent(Settings.ACTION_FINGERPRINT_ENROLL))
                }, 5000
            )

        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            Toast.makeText(activity, "Please enroll a fingerprint ", Toast.LENGTH_LONG).show()

            //Let's give time to Toast to disappear before sending the user to fingerprint
            // registration page
            Handler().postDelayed(
                {
                    //Implicit intent sending user to register a fingerprint for versions < 28 and >= 23
                    activity.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
                }, 5000
            )

        }else{
            //Toast.makeText(mContext, "Your device does not support biometrics", Toast.LENGTH_LONG).show()

        }
    }
}