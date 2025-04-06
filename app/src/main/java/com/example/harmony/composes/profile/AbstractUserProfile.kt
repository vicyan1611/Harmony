package com.example.harmony.composes.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.harmony.composes.RoundedAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileLayout(displayedName: String = "", username: String = "", bio: String = "", modifier: Modifier, onDismissRequest: () -> Unit, headerContent: @Composable RowScope.() -> Unit, bodyContent: @Composable RowScope.() -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier
            .fillMaxWidth(),
//            .heightIn(max = LocalConfiguration.current.screenHeightDp.dp * 0.2f), // set max height when fully expand, but it does not work ??
        // sheetState = rememberModalBottomSheetState(true), // skip partially expand
    ) {
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // interactive buttons on top
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()){
                headerContent()
            }

            // user info: avatar, name, username, bio
            Row (
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    RoundedAvatar(size = 64.dp, char = displayedName.getOrElse(0) { ' ' }.uppercaseChar())
                    Text(
                        text = displayedName,
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = username,
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        )

                        Box(modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color.Green), contentAlignment = Alignment.Center) {
                            Text(
                                text = "#",
                                style = TextStyle(
                                    color = Color.Black,
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            // body
            Row (
                modifier = Modifier.fillMaxWidth()
            ) {
                bodyContent()
            }
        }

    }
}