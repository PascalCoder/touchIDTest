package com.thepascal.touchidtest

import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.thepascal.login.FingerprintAuthHelp
import kotlinx.android.synthetic.main.activity_testing.*

class TestingActivity : AppCompatActivity() {

    private lateinit var fingerprintAuthHelp: FingerprintAuthHelp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_testing)

        fingerprintAuthHelp = FingerprintAuthHelp(this)
        //fingerprintAuthHelp.init()
        startFingerPrintAuthHelper()

        if(Build.VERSION.SDK_INT >= 23)
            fingerprintAuthHelp.initKeyStore()

        btnSavePwd.setOnClickListener {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
               fingerprintAuthHelp.savePassword(etPasswordT.text.toString(), CancellationSignal(), getAuthListener(false))
        }

        btnGetPwd.setOnClickListener {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                fingerprintAuthHelp.getPassword(CancellationSignal(), getAuthListener(true))
        }
    }

    // Start the finger print helper. In case this fails show error to user
    private fun startFingerPrintAuthHelper(){
        fingerprintAuthHelp = FingerprintAuthHelp(this)

        if(!fingerprintAuthHelp.init()){
            tvErrorText.text = fingerprintAuthHelp.getLastError()
        }
    }

    @NonNull
    private fun getAuthListener(isGetPass: Boolean): FingerprintAuthHelp.Callback{

        return object: FingerprintAuthHelp.Callback{
            override fun onSuccess(savedPass: String) {
                if(isGetPass){
                    tvErrorText.text = "Success!! Pass = $savedPass"
                }else{
                    tvErrorText.text = "Encrypted pass = $savedPass"
                }
            }

            override fun onFailure(message: String) {
                tvErrorText.text = "Failed - $message"
            }

            override fun onHelp(helpCode: Int, helpString: String) {
                tvErrorText.text = "Help need - $helpString"
            }

        }
    }
}
