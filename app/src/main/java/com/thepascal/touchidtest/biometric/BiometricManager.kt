package com.thepascal.touchidtest.biometric

import android.annotation.TargetApi
import android.content.Context
import android.content.DialogInterface
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
//import android.os.CancellationSignal
import com.thepascal.touchidtest.BiometricUtils

class BiometricManager(var biometricBuilder: BiometricBuilder): BiometricManagerV23() { //https://github.com/anitaa1990/Biometric-Auth-Sample/blob/13b8679d0561ce13e1ba3b063dae091e11602df8/biometric-auth/src/main/java/com/an/biometric/BiometricManager.java

    init {
        this.context = biometricBuilder.context
        this.title = biometricBuilder.title
        this.subtitle = biometricBuilder.subtitle
        this.description = biometricBuilder.description
        this.negativeButtonText = biometricBuilder.negativeButtonText
    }

    val mCancellationSignal = CancellationSignal()

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
                            BiometricCallbackV28(biometricCallback))
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

        fun build(): BiometricManager{
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