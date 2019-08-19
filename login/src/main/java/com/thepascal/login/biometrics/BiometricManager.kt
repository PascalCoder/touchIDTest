package com.thepascal.login.biometrics

import android.annotation.TargetApi
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import android.os.Handler
import android.provider.Settings
import android.widget.Toast
import com.thepascal.touchidtest.BiometricManagerV23

class BiometricManager(biometricBuilder: BiometricBuilder): BiometricManagerV23() {

    init {
        this.context = biometricBuilder.mContext
        this.title = biometricBuilder.title
        this.subtitle = biometricBuilder.subtitle
        this.description = biometricBuilder.description
        this.negativeButtonText = biometricBuilder.negativeButtonText
    }

    private val mCancellationSignal = CancellationSignal()

    fun authenticate(biometricCallback: BiometricCallback){
        if(title == "" || subtitle == "" || description == "" ||
                negativeButtonText == ""){
            biometricCallback.onBiometricAuthenticationInternalError("Biometric Dialog title cannot be null")
        }

        if (!BiometricUtils.isSdkVersionSupported()){
            biometricCallback.onSdkVersionNotSupported()
        }

        if(!BiometricUtils.isPermissionGranted(context)){
            biometricCallback.onBiometricAuthenticationPermissionNotGranted()
        }

        if(!BiometricUtils.isHardwareSupported(context)){
            biometricCallback.onBiometricAuthenticationNotSupported()
        }

        if(!BiometricUtils.isFingerprintAvailable(context)){
            biometricCallback.onBiometricAuthenticationNotAvailable()

            promptUserToRegisterFingerprint()
        }

        if (BiometricUtils.isFingerprintAvailable(context))
            displayBiometricDialog(biometricCallback)
    }

    fun cancelAuthentication(){
        if(BiometricUtils.isBiometricEnabled()){
            if(!mCancellationSignal.isCanceled) mCancellationSignal.cancel()
        }else{
            if (!mCancellationSignalV23.isCanceled) mCancellationSignalV23.cancel()
        }
    }

    private fun displayBiometricDialog(biometricCallback: BiometricCallback)
        = if(BiometricUtils.isBiometricEnabled()) displayBiometricPrompt(biometricCallback)
            else displayBiometricPromptV23(biometricCallback)

    @TargetApi(Build.VERSION_CODES.P)
    fun displayBiometricPrompt(biometricCallback: BiometricCallback){
        BiometricPrompt.Builder(context)
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)
            .setNegativeButton(negativeButtonText, context.mainExecutor,
                object : DialogInterface.OnClickListener{
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        biometricCallback.onAuthenticationCancelled()
                    }

                })
            .build()
            .authenticate(
                CancellationSignal(), context.mainExecutor,
                BiometricCallbackV28(biometricCallback)
            )
    }

    private fun promptUserToRegisterFingerprint(){
        //prompt the user to register a fingerprint for versions >= 28
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Toast.makeText(context, "Please enroll a fingerprint", Toast.LENGTH_LONG).show()

            //Let's give time to Toast to disapear before sending the user to fingerprint
            // registration page
            Handler().postDelayed(
                {
                    //Implicit intent sending user to register a fingerprint for versions >= 28
                    context.startActivity(Intent(Settings.ACTION_FINGERPRINT_ENROLL))
                }, 5000
            )

        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            Toast.makeText(context, "Please enroll a fingerprint ", Toast.LENGTH_LONG).show()

            //Let's give time to Toast to disappear before sending the user to fingerprint
            // registration page
            Handler().postDelayed(
                {
                    //Implicit intent sending user to register a fingerprint for versions < 28 and >= 23
                    context.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
                }, 5000
            )

        }else{
            //Toast.makeText(mContext, "Your device does not support biometrics", Toast.LENGTH_LONG).show()

        }
    }

    //var mCancellationSignal = CancellationSignal()

    class BiometricBuilder(context: Context){

        lateinit var title: String
        lateinit var subtitle: String
        lateinit var description: String
        lateinit var negativeButtonText: String

        var mContext: Context = context

        /*init {
            this.mContext = context
        }*/

        fun setTitle(title: String): BiometricBuilder {
            this.title = title
            return this
        }

        fun setSubtitle(subtitle: String): BiometricBuilder {
            this.subtitle = subtitle
            return this
        }

        fun setDescription(description: String): BiometricBuilder {
            this.description = description
            return this
        }

        fun setNegativeButtonText(negativeButtonText: String): BiometricBuilder {
            this.negativeButtonText = negativeButtonText
            return this
        }

        fun build(): BiometricManager {
            return BiometricManager(this)
        }

    }

}