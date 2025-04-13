package com.example.harmony.composes.fragments

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.harmony.R

data class NavigationItem(
    val icon: Painter,
    val label: String,
    val route: String
)


@Preview
@Composable
fun NavBar(
    modifier: Modifier = Modifier,
) {
    val navigationItems = listOf(
        NavigationItem(
            icon = painterResource(id = R.drawable.home),
            label = "Home",
            route = "home"
        ),
        NavigationItem(
            icon = painterResource(id = R.drawable.notifications),
            label = "Notifications",
            route = "notifications"
        ),
        NavigationItem(
            icon = painterResource(id = R.drawable.account),
            label = "Profile",
            route = "profile"
        ),
    )
    val selectedItem = remember { mutableStateOf(navigationItems[0]) }
    val selectedItemIndex = remember { mutableStateOf(0) }
    val selectedItemColor = colorResource(id = R.color.primary_blurple)
    val unselectedItemColor = colorResource(id = R.color.primary_gray_bright)
    val backgroundColor = colorResource(id = R.color.primary_gray)
    val iconSize = 32.dp
    NavigationBar(
        modifier = modifier,
        containerColor = colorResource(R.color.primary_gray_light)
    ) {
        navigationItems.forEachIndexed { index, item ->
            val isSelected = selectedItemIndex.value == index
            val iconColor = if (isSelected) selectedItemColor else unselectedItemColor
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .clickable {
                        selectedItem.value = item
                        selectedItemIndex.value = index
                    }
                    .padding(8.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(
                        if (isSelected) selectedItemColor.copy(alpha = 0.5f) else backgroundColor.copy(
                            alpha = 0.5f
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = item.icon,
                    contentDescription = item.label,
                    modifier = Modifier
                        .padding(16.dp)
                        .width(iconSize)
                        .height(iconSize)
                        .clickable {
                            selectedItem.value = item
                            selectedItemIndex.value = index
                        },
                    colorFilter = ColorFilter.tint(iconColor),
                    contentScale = ContentScale.Fit,
                    alignment = Alignment.Center,
                    alpha = if (isSelected) 1f else 0.5f,
                )
                Text(
                    text = item.label,
                    color = colorResource(R.color.primary_gray_bright),
                    fontSize = 10.sp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(vertical = 4.dp, horizontal = 8.dp),
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}