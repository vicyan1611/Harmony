package com.example.harmony.composes

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.harmony.composes.ui.theme.HarmonyTheme

class Profile : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var showBottomSheet by remember { mutableStateOf(false) }
            HarmonyTheme(isLightMode = false) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Row (modifier = Modifier.padding(top = 30.dp, start = 10.dp, end = 10.dp).fillMaxSize()) {
                        Button(onClick = {
                            showBottomSheet = true
                        }) {
                            Text(text = "Test")
                            // Icon(Icons.Filled.Add, contentDescription = "click to show modal btm sheet")
                        }
                        if (showBottomSheet) {
                            OtherUserProfile (
                                displayedName = "ketamean",
                                username = "_ketamean",
                                isFriend = true,
                                modifier = Modifier.fillMaxSize(),
                                onDismissRequest = {
                                    showBottomSheet = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun Preview() {
    var showBottomSheet by remember { mutableStateOf(false) }
    Scaffold(modifier = Modifier.fillMaxSize().padding(top = 30.dp, start = 10.dp, end = 10.dp)) { innerPadding ->
        Button(onClick = {
            showBottomSheet = true
        }) {
            Text(text = "Test")
            // Icon(Icons.Filled.Add, contentDescription = "click to show modal btm sheet")
        }
        if (showBottomSheet) {
            OtherUserProfile (
                displayedName = "ketamean",
                username = "_ketamean",
                isFriend = true,
                modifier = Modifier.fillMaxSize(),
                onDismissRequest = {
                    showBottomSheet = false
                }
            )
        }
    }
}