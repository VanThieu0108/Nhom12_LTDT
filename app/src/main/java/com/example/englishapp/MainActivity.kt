package com.example.englishapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.englishapp.navigation.AppNavigation
import com.example.englishapp.navigation.MainActivityNavigation

class MainActivity : AppCompatActivity(), MainActivityNavigation {
    override lateinit var appNavigation: AppNavigation
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        appNavigation = AppNavigation(this)
        appNavigation.start()
    }
}
