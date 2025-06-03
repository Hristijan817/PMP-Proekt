package com.example.eating.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.eating.R
import com.example.eating.fragments.LoginFragment

class AuthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        supportFragmentManager.beginTransaction()
            .replace(R.id.auth_fragment_container, LoginFragment())
            .commit()
    }
}
