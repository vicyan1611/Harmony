package com.example.harmony.composes

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

@Composable
fun RoundedContainer(
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    content: @Composable BoxScope.() -> Unit
) {
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
fun RoundedButton(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    onClick: () -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.secondary,
    contentColor: Color = MaterialTheme.colorScheme.onSecondary,
    content: @Composable RowScope.() -> Unit
) {
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
fun RoundedAvatar(
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    avatarImageUrl: String = "",
    char: Char = ' '
) {
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
fun TextBox(
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    maxLines: Int = 1,
    editable: Boolean = false,
    text: String = "",
    textStyle: TextStyle = LocalTextStyle.current,
    onValueChange: (String) -> Unit = {},
    label: String = "",
    showCharsCounter: Boolean = false,
    maxChars: Int = Int.MAX_VALUE,
    colors: TextFieldColors = TextFieldDefaults.colors(
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
    )
) {
    Box(modifier = modifier) {
        OutlinedTextField(
            value = text,
            onValueChange = onValueChange,
            modifier = modifier,
            readOnly = !editable,
            textStyle = textStyle,
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

fun ShareText(textToShare: String, context: Context) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, textToShare)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, null) // Title for the chooser
    context.startActivity(shareIntent, null)
}