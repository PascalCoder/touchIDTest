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
        this.context = biometricBuilder.context
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

            //prompt the user to register a fingerprint for versions >= 28
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                Toast.makeText(context, "Please enroll a fingerprint", Toast.LENGTH_LONG).show()

                Handler().postDelayed(
                    {
                        //Implicit intent sending user to register a fingerprint for versions >= 28
                        context.startActivity(Intent(Settings.ACTION_FINGERPRINT_ENROLL))
                    }, 5000
                )
            }else{
                Toast.makeText(context, "Please enroll a fingerprint ", Toast.LENGTH_LONG).show()
                Handler().postDelayed(
                    {
                        //Implicit intent sending user to register a fingerprint for versions < 28 and >= 23
                        context.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
                    }, 5000
                )

            }

        }

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

    //var mCancellationSignal = CancellationSignal()

    class BiometricBuilder(context: Context){

        lateinit var title: String
        lateinit var subtitle: String
        lateinit var description: String
        lateinit var negativeButtonText: String

        var context: Context

        init {
            this.context = context
        }

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

    /*companion object{

        class BiometricBuilder(context: Context){

            lateinit var title: String
            lateinit var subtitle: String
            lateinit var description: String
            lateinit var negativeButtonText: String

            var context: Context

            init {
                this.context = context
            }

            fun setTitle(title: String): BiometricBuilder{
                this.title = title
                return this
            }

            fun setSubtitle(subtitle: String): BiometricBuilder{
                this.subtitle = subtitle
                return this
            }

            fun setDescription(description: String): BiometricBuilder{
                this.description = description
                return this
            }

            fun setNegativeButtonText(negativeButtonText: String): BiometricBuilder{
                this.negativeButtonText = negativeButtonText
                return this
            }

            *//*fun build(): BiometricManager{
                return BiometricManager(this)
            }*//*

        }
    }*/
}