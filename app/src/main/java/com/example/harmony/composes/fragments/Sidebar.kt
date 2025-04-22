package com.example.harmony.composes.fragments

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.harmony.R

@Preview
@Composable
fun Sidebar(
    modifier: Modifier = Modifier,
) {
    ConstraintLayout(
        modifier = modifier
    ) {
        val (mainBtn, divider, addServer) = createRefs()
        val isClicked = remember { mutableStateOf(false) }
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(60.dp)
                .clip(
                    if (!isClicked.value) {
                        RoundedCornerShape(30.dp)
                    } else {
                        RoundedCornerShape(15.dp)
                    }
                )
                .background(
                    if (!isClicked.value) {
                        colorResource(id = R.color.primary_gray_light)
                    } else {
                        colorResource(id = R.color.primary_blurple)
                    }
                )
                .constrainAs(mainBtn) {
                    top.linkTo(parent.top, 30.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .clickable {
                    isClicked.value = !isClicked.value
                }
        ) {
            Image(
                painter = painterResource(id = R.drawable.discord_symbol_white),
                contentDescription = null,
                modifier = Modifier
                    .width(40.dp)
                    .height(40.dp)
                    .background(
                        if (!isClicked.value) {
                            colorResource(id = R.color.primary_gray_light)
                        } else {
                            colorResource(id = R.color.primary_blurple)
                        }
                    )
                    .align(Alignment.Center),
            )
        }
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(divider) {
                    top.linkTo(mainBtn.bottom, 10.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .padding(horizontal = 5.dp),
            color = colorResource(id = R.color.primary_gray_bright)
        )
        // each channel will be the same as this but with different icon/image
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(60.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(colorResource(id = R.color.primary_gray_light))
                .constrainAs(addServer) {
                    top.linkTo(divider.bottom, 10.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        ) {
            Image(
                painter = painterResource(id = R.drawable.plus),
                contentDescription = null,
                modifier = Modifier
                    .width(20.dp)
                    .height(20.dp)
                    .background(colorResource(id = R.color.primary_gray_light))
                    .align(Alignment.Center),
                colorFilter = ColorFilter.tint(
                    color = colorResource(id = R.color.primary_green)
                ),
                contentScale = ContentScale.Fit,
            )
        }
    }
}