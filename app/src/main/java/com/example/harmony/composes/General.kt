package com.example.harmony.composes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage


@Composable
fun RoundedContainer(modifier: Modifier = Modifier, size: Dp = 80.dp, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondary),
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Composable
fun RoundedButton(modifier: Modifier = Modifier, size: Dp = 40.dp, onClick: () -> Unit, containerColor: Color = MaterialTheme.colorScheme.secondary, contentColor: Color = MaterialTheme.colorScheme.onSecondary, content: @Composable RowScope.() -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContentColor = Color.LightGray,
            disabledContainerColor = Color.LightGray
        ),
        shape = CircleShape,
        modifier = Modifier.size(size),
        contentPadding = PaddingValues(2.dp),
        content = content
    )
}

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

@Composable
fun MultiLineTextField(modifier: Modifier = Modifier, editable: Boolean = true, onValueChange: (String) -> Unit, text: String, textStyle: TextStyle = TextStyle(), maxLines: Int = 6) {
//    OutlinedTextField(
//        value = text,
//        modifier = modifier
//            .fillMaxWidth(),
//        onValueChange = onValueChange,
//        readOnly = !editable,
//        textStyle = textStyle,
//        shape = RoundedCornerShape(12.dp),
//        singleLine = false,
//        maxLines = 8
//    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.secondary)
    ) {
        Text(
            text = text,
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 12.dp),
            style = textStyle,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis
        )
    }
}