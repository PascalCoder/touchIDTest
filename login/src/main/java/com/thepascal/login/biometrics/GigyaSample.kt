/*package com.thepascal.login.biometrics

import android.widget.Toast

fun onOptInButtonClicked(){
    val fpCallbacks: IFingerprintCallbacks = object: IFingerprintCallbacks{
        override fun onNotSupported(reason: GigyaFingerprintManager.FingerprintError) {
            //Display to user the reason this issue occurred
        }

        override fun onError(e: Exception) {
            //Show user the error that is preventing authentication
        }

        override fun onSuccess() {
            //Hide fingerprint dialog
            //Show a 'successful' message to user
            //Send user to the next screen
        }

    }

    val fpOperation: IFingerprintOperation = GSApi.getInstance().fingerprint.optIn(fpCallbacks)

    showMyFingerprintDialog(object: IMyCallback{
        override fun onCancel(){
            fpOperation.cancel()
        }
    })
}

interface IMyCallback {
    fun onCancel()
}

fun showMyFingerprintDialog(any: Any) {

}

class GSApi {
    val fingerprint = ""
    companion object getInstance{
        fun getInstance(): GSApi{
            return GSApi()
        }
    }
}

class IFingerprintOperation {

    fun cancel(){}
}

interface IFingerprintCallbacks {

    fun onNotSupported(reason: GigyaFingerprintManager.FingerprintError)
    fun onError(e: Exception)
    fun onSuccess()

}

class GigyaFingerprintManager {

    inner class FingerprintError{

    }
}
*/