package com.example.harmony.core.components

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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

@Preview
@Composable
fun RoundedButtonPreview() {
    RoundedButton(onClick = {}, size = 80.dp) {
        Text(text = "Hello")
    }
}