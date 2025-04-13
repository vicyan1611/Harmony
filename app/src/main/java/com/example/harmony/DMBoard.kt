package com.example.harmony

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout

@Composable
fun ChatItem() {
    ConstraintLayout(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(
                width = 1.dp,
                color = colorResource(id = R.color.primary_gray_light),
                shape = RoundedCornerShape(10.dp)
            )
            .height(60.dp)
            .fillMaxWidth()
    ) {
        val (profile, info) = createRefs()
        Image(
            painter = painterResource(id = R.drawable.account),
            contentDescription = "Profile",
            modifier = Modifier
                .width(48.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(64.dp))
                .constrainAs(profile) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(info.start, 8.dp)
                },
            contentScale = ContentScale.Fit,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp)
                .constrainAs(info) {
                    top.linkTo(parent.top)
                    start.linkTo(profile.end, 8.dp)
                }
        ){
            Text(
                text = "User Name",
                fontSize = 16.sp,
                fontFamily = FontFamily(Font(R.font.ggsans_bold)),
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.primary_gray_bright),
            )
            Row{
                Text(
                    text = "Last message",
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(R.font.ggsans)),
                    fontWeight = FontWeight.Normal,
                    color = colorResource(id = R.color.primary_gray_bright),
                )
                Text(
                    text = "12:00",
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(R.font.ggsans)),
                    fontWeight = FontWeight.Normal,
                    color = colorResource(id = R.color.primary_gray_bright),
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .align(Alignment.CenterVertically)
                )
            }
        }
    }
}

@Preview
@Composable
fun DMBoard(
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val (title, searchBar, chats) = createRefs()
        Text(
            text = "Messages",
            fontSize = 20.sp,
            fontFamily = FontFamily(Font(R.font.ggsans_bold)),
            fontWeight = FontWeight.Bold,
            color = colorResource(id = R.color.primary_gray_bright),
            modifier = Modifier
                .constrainAs(title) {
                    top.linkTo(parent.top, 20.dp)
                    start.linkTo(parent.start, 10.dp)
                }
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .constrainAs(searchBar) {
                    top.linkTo(title.bottom, 16.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
        ) {
            var text by remember { mutableStateOf("") }
            Icon(
                Icons.Rounded.Search,
                contentDescription = "Search",
                tint = colorResource(id = R.color.primary_gray_bright),
                modifier = Modifier
                    .height(36.dp)
                    .width(36.dp)
                    .padding(start = 16.dp)
                    .align(Alignment.CenterVertically)
            )
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Search") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(48.dp),
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .constrainAs(chats) {
                    top.linkTo(searchBar.bottom, 16.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        ) {
            ChatItem()
            ChatItem()
            ChatItem()
            ChatItem()
            ChatItem()
            ChatItem()
        }
    }
}