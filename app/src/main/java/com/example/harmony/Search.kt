package com.example.harmony

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension

class Search : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorResource(id = R.color.primary_gray))
            ) {
                SearchHeader()
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    SearchBody()
                }
            }
        }
    }
}

//@Preview(
//    showBackground = true,
//    showSystemUi = true,
//)
@Composable
fun SearchHeader() {
    val primaryGray = colorResource(id = R.color.primary_gray)
    var text by remember { mutableStateOf("") }
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(primaryGray)
    ) {
        val (backButton, searchField) = createRefs()
        Icon(
            imageVector = Icons.Rounded.ArrowBackIosNew,
            contentDescription = "Back",
            modifier = Modifier
                .padding(16.dp)
                .size(24.dp)
                .clickable { /* Handle back action */ }
                .constrainAs(backButton) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                },
            tint = Color.White
        )
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Search something") },
            colors =
                TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.White,
                    unfocusedIndicatorColor = Color.White,
                    focusedContainerColor = primaryGray,
                    unfocusedContainerColor = primaryGray,
                    cursorColor = Color.White,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                ),
            placeholder = {
                Text(
                    text = "Someone is waiting for you...",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 16.sp,
                    fontFamily = ggsans,
                    fontWeight = FontWeight.Normal,
                )
            },
            modifier = Modifier
                .height(60.dp)
                .constrainAs(searchField) {
                    start.linkTo(backButton.end, margin = 16.dp)
                    end.linkTo(parent.end, margin = 16.dp)
                    bottom.linkTo(parent.bottom, margin = 16.dp)
                    width = Dimension.fillToConstraints
                },
        )
    }
}

@Composable
fun SearchItem() {
    val placeholder = painterResource(id = R.drawable.account)
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(horizontal = 4.dp, vertical = 8.dp)
            .clickable { /* TODO: Handle click action */ }
            .padding(8.dp)
            .clip(shape = RoundedCornerShape(8.dp))
            .background(colorResource(id = R.color.primary_gray_light))
    ) {
        val (avatar, messageContent) = createRefs()
        Image(
            painter = placeholder,
            contentDescription = "Placeholder",
            modifier = Modifier
                .size(64.dp)
                .constrainAs(avatar) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                },
            contentScale = ContentScale.Crop,
            alpha = 0.5f
        )
        Column(
            modifier = Modifier
                .constrainAs(messageContent) {
                    start.linkTo(avatar.end, margin = 16.dp)
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                }
                .padding(8.dp)
        ) {
            Text(
                text = "Name",
                fontSize = 16.sp,
                fontFamily = ggsans,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                    fontSize = 14.sp,
                    fontFamily = ggsans,
                    fontWeight = FontWeight.Normal,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                Text(
                    text = "12:00",
                    fontSize = 12.sp,
                    fontFamily = ggsans,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.5f),
                )
            }
        }
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
)
@Composable
fun SearchBody() {
    val primaryGray = colorResource(id = R.color.primary_gray)
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(primaryGray)
    ) {
        items(10) { index ->
            SearchItem()
        }
    }
}