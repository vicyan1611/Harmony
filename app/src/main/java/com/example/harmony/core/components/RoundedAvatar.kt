package com.example.harmony.core.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.harmony.core.theme.HarmonyTheme

@Composable
fun RoundedAvatar(modifier: Modifier = Modifier, size: Dp = 80.dp, avatarImageUrl: String = "", char: Char = ' ') {
    RoundedContainer(modifier = modifier, size = size) {
        if (avatarImageUrl.trim() == "") {
            Text(
                text = char.toString(),
                style = TextStyle(
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontSize = 24.sp
                )
            )
        } else {
            AsyncImage(
                model = avatarImageUrl,
                contentDescription = "",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
        }
    }
}

@Preview
@Composable
fun RoundedAvatarPreview() {
    HarmonyTheme {
        RoundedAvatar(avatarImageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQJxo2NFiYcR35GzCk5T3nxA7rGlSsXvIfJwg&s", char = 'S')
    }
}