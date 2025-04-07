package com.example.harmony

import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.harmony.composes.server.ServerCreation
import com.example.harmony.composes.ui.theme.HarmonyTheme

class ServerCreation : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HarmonyTheme(isLightMode = false) {
                Scaffold (
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Column (
                        modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding(), start = 8.dp, end = 8.dp)
                    ) {
                        ServerCreation()
                    }
                }
            }
        }
    }
}