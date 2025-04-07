package com.example.harmony.composes.test

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
import com.example.harmony.composes.channel.ChannelConfigMenus
import com.example.harmony.composes.profile.MyProfile
import com.example.harmony.composes.profile.OtherUserProfile
import com.example.harmony.composes.ui.theme.HarmonyTheme
import kotlin.uuid.ExperimentalUuidApi

class Profile : ComponentActivity() {
    @OptIn(ExperimentalUuidApi::class)
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var showBottomSheet_other by remember { mutableStateOf(false) }
            var showBottomSheet_mine by remember { mutableStateOf(false) }
            var channelSettingSheet by remember { mutableStateOf(false) }
            var showServerCreation by remember { mutableStateOf(false) }
            HarmonyTheme(isLightMode = false) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Row (modifier = Modifier.padding(top = 30.dp, start = 10.dp, end = 10.dp).fillMaxSize()) {
                        Button(onClick = {
                            showBottomSheet_other = true
                        }) {
                            Text(text = "Test other")
                        }
                        Button(onClick = {
                            showBottomSheet_mine = true
                        }) {
                            Text(text = "Test mine")
                        }

                        Button(onClick = {
                            channelSettingSheet = true
                        }) {
                            Text(text = "Test channel settings")
                        }

                        Button(onClick = {
                            showServerCreation = true
                        }) {
                            Text(text = "Test server creation")
                        }

                        if (showBottomSheet_other) {
                            OtherUserProfile (
                                displayedName = "ketamean",
                                username = "_ketamean",
                                isFriend = true,
                                bio = "Hello, I'm ketamean Hello, I'm ketamean Hello, I'm ketamean Hello, I'm ketamean Hello, I'm ketamean Hello, I'm ketamean Hello, I'm ketamean Hello, I'm ketamean Hello, I'm ketamean Hello, I'm ketamean Hello, I'm ketamean Hello, I'm ketamean Hello, I'm ketamean Hello, I'm ketamean Hello, I'm ketamean Hello, I'm ketamean Hello, I'm ketamean Hello, I'm ketamean Hello, I'm ketamean Hello, I'm ketamean",
                                modifier = Modifier.fillMaxSize(),
                                onDismissRequest = {
                                    showBottomSheet_other = false
                                }
                            )
                        }

                        if (showBottomSheet_mine) {
                            MyProfile (
                                displayedName = "ketamean",
                                username = "_ketamean",
                                bio = "Hello, I'm ketamean Hello, I'm ketamean Hello, I'm ketamean Hello, I'm ketamean Hello, I'm ketamean Hello, I'm ketamean Hello, I'm ketamean Hello, I'm ketamean Hello, I'm ketamean Hello, I'm ketamean Hello, I'm ketamean Hello, I'm ketamean Hello, I'm ketamean Hello, I'm ketamean Hello, I'm ketamean Hello, I'm ketamean Hello, I'm ketamean Hello, I'm ketamean Hello, I'm ketamean Hello, I'm ketamean",
                                modifier = Modifier.fillMaxSize(),
                                avatarUrl = "https://cdn-imgix.headout.com/tour/7064/TOUR-IMAGE/b2c74200-8da7-439a-95b6-9cad1aa18742-4445-dubai-img-worlds-of-adventure-tickets-02.jpeg?auto=format&w=900&h=562.5&q=90&fit=crop&ar=16%3A10",
                                onDismissRequest = {
                                    showBottomSheet_mine = false
                                }
                            )
                        }

                        if (channelSettingSheet) {
                            ChannelConfigMenus (
                                modifier = Modifier.fillMaxSize(),
                                channelName = "#hehe",
                                channelDescription = "Đây là channel mà tôi đã tạo. Đây là channel mà tôi đã tạo. Đây là channel mà tôi đã tạo. Đây là channel mà tôi đã tạo. Đây là channel mà tôi đã tạo. Đây là channel mà tôi đã tạo.",
                                onDismissRequest = {
                                    channelSettingSheet = false
                                }
                            )
                        }

                        if (showServerCreation) {

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
    var showBottomSheet_other by remember { mutableStateOf(false) }
    Scaffold(modifier = Modifier.fillMaxSize().padding(top = 30.dp, start = 10.dp, end = 10.dp)) { innerPadding ->
        Button(onClick = {
            showBottomSheet_other = true
        }) {
            Text(text = "Test")
            // Icon(Icons.Filled.Add, contentDescription = "click to show modal btm sheet")
        }
        if (showBottomSheet_other) {
            OtherUserProfile (
                displayedName = "ketamean",
                username = "_ketamean",
                isFriend = true,
                modifier = Modifier.fillMaxSize(),
                onDismissRequest = {
                    showBottomSheet_other = false
                }
            )
        }
    }
}