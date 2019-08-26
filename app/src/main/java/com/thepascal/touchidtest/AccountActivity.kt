package com.thepascal.touchidtest

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_account.*

class AccountActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        btnLogout.setOnClickListener {
            logOut()
        }
    }

    fun logOut(){
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
