package com.ravimhzn.cleanarchitecture_notes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.ravimhzn.cleanarchitecture_notes.framework.presentation.BaseApplication
import com.ravimhzn.cleanarchitecture_notes.utils.printLogD
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    private val TAG = "AppDebug ->"

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as BaseApplication).appComponent.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        printLogD("MainActivity", "FirebaseAuth : ${firebaseAuth}")
    }
}
