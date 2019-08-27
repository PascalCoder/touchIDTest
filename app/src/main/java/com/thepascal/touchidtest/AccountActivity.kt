package com.thepascal.touchidtest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_account.*

class AccountActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        val sharedPrefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val decryptedPassword = ""//intent?.getStringExtra("pass")

        if(decryptedPassword != null){
            Toast.makeText(this, "Your password is $decryptedPassword", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this, "Your password is null", Toast.LENGTH_SHORT).show()
        }

        btnLogout.setOnClickListener {
            logOut()
        }
    }

    fun logOut(){
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
