package com.example.harmony

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.example.harmony.composes.test.Profile

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_main)
//        val intent = Intent(this, PollCreation::class.java)
//        val intent = Intent(this, ServerCreation::class.java)
//        val intent = Intent(this, Search::class.java)
//        val intent = Intent(this, DirectMessage::class.java)
//        startActivity(intent)
    }
}