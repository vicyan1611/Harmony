package com.example.harmony.composes

import android.text.Editable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
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
fun TextBox(modifier: Modifier = Modifier, minLines: Int = 1, maxLines: Int = 1, editable: Boolean = false, text: String = "", style: TextStyle = LocalTextStyle.current, onValueChange: (String) -> Unit = {}, label: String = "", showCharsCounter: Boolean = false, maxChars: Int = Int.MAX_VALUE) {
    Box(modifier = modifier) {
        OutlinedTextField(
            value = text,
            onValueChange = onValueChange,
            modifier = modifier,
            readOnly = !editable,
            textStyle = style,
            singleLine = (maxLines == 1),
            minLines = minLines,
            maxLines = maxLines,
            label = {
                Text(
                    text = label
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.tertiary,
                unfocusedContainerColor = MaterialTheme.colorScheme.tertiary,
                disabledContainerColor = MaterialTheme.colorScheme.tertiary,
                focusedTextColor = MaterialTheme.colorScheme.onTertiary,
                unfocusedTextColor = MaterialTheme.colorScheme.onTertiary,
                disabledTextColor = MaterialTheme.colorScheme.onTertiary,
                focusedLabelColor = MaterialTheme.colorScheme.onTertiary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onTertiary,
                disabledLabelColor = MaterialTheme.colorScheme.onTertiary,
                cursorColor = MaterialTheme.colorScheme.onTertiary,
                focusedIndicatorColor = MaterialTheme.colorScheme.onSecondary
            ),
            shape = RoundedCornerShape(16.dp)
        )
        if (showCharsCounter && maxChars < Int.MAX_VALUE) {
            Text(
                text = "${maxChars - text.length}",
                style = TextStyle(
                    fontSize = 12.sp
                ),
                modifier = Modifier
                    .padding(end = 8.dp, bottom = 8.dp)
                    .align(Alignment.BottomEnd)
            )
        }

    }
}

@Composable
fun MyTextField(modifier: Modifier = Modifier, text: String = "", onValueChange: (String) -> Unit = {}, label: String = "") {
    TextField(
        modifier = modifier,
        value = text,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label
            )
        },
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.secondary,
            unfocusedContainerColor = MaterialTheme.colorScheme.secondary,
            disabledContainerColor = MaterialTheme.colorScheme.secondary,
            focusedTextColor = MaterialTheme.colorScheme.onSecondary,
            unfocusedTextColor = MaterialTheme.colorScheme.onSecondary,
            disabledTextColor = MaterialTheme.colorScheme.onSecondary,
            focusedLabelColor = MaterialTheme.colorScheme.onSecondary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSecondary,
            disabledLabelColor = MaterialTheme.colorScheme.onSecondary,
            cursorColor = MaterialTheme.colorScheme.onSecondary
        )
    )
}