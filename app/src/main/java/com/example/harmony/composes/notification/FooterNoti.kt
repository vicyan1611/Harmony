package com.example.harmony.composes.notification

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// footer navigation
@Composable
fun FooterNavigation(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(
        containerColor = Color(0xFF2E3136),
        contentColor = Color.White
    ) {
        NavigationBarItem(
            icon = {
                BadgedBox(
                    badge = {
                        if (selectedTab != 0) {
                            Badge { Text("261") }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home"
                    )
                }
            },
            label = { Text("Home") },
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Notifications, contentDescription = "Notifications") },
            label = { Text("Notifications") },
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "You") },
            label = { Text("You") },
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) }
        )
    }
}