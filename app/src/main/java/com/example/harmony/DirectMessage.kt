package com.example.harmony

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Adjust
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
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
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.char
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.until
import java.net.URL
import android.util.Log
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import coil3.compose.rememberAsyncImagePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

data class DateTime(
    val date: String,
    val time: String = "12:00:00",
    val timezone: String = "Asia/Ho_Chi_Minh",
    val dateTime: String = "12:00:00 01-10-2023",
)

data class Message(
    val text: String,
    val isUser: Boolean = false,
    val isEmoji: Boolean = false,
    val isImage: Boolean = false, // contain at least 1 image
    val isAudio: Boolean = false,
    val isFile: Boolean = false,
    val isLink: Boolean = false,
    val imagesList: List<Painter> = emptyList(),
    val senderName: String = "Username",
    val sentTime: DateTime = DateTime(
        date = "29-03-2025",
        time = "00:00:00",
        timezone = "Asia/Ho_Chi_Minh",
        dateTime = "00:00:00 29-03-2020"
    ),
)

fun extractFirstLink(text: String): String {
    // Regex for matching URLs with http, https, or ftp protocols
    val regex = "(?:^|\\s)(?:(?:https?|ftp)://)?(?:www\\.)?[a-zA-Z0-9-]+\\.[a-zA-Z]{2,}(?::\\d{1,5})?(?:[/?#][^\\s]*)?(?=$|\\s)"
    val matchResult = regex.toRegex().find(text)
    return matchResult?.value!!.trim() ?: ""
}

fun openLinkInBrowser(context: Context, url: String) {
    try {
        val normalizedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "https://$url"
        } else {
            url
        }

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(normalizedUrl))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (e: Exception) {
        Log.e("OpenLink", "Error opening link: ${e.message}")
    }
}

class DirectMessage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("Check Extract: ", extractFirstLink("Here is the link to the project: www.example.com"))
        setContent {
            Column(modifier = Modifier.fillMaxSize()) {
                val primaryGrayBright = colorResource(
                    R.color.primary_gray_bright
                )
                Header()
                BodyInfo()
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(0.5f),
                    color = primaryGrayBright,
                    thickness = 1.dp,
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(primaryGrayBright)
                ) {
                    BodyMessage()
                }
                Footer()
            }
        }
    }
}

@Preview(
    name = "Chat Header",
    showBackground = true,
)
@Composable
fun Header() {
    val primaryGray = colorResource(id = R.color.primary_gray)
    val primaryGrayBright = colorResource(id = R.color.primary_gray_bright)
    val avatar = painterResource(id = R.drawable.account)
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .background(primaryGray)
            .height(120.dp)
    ) {
        val (backRef, avatarGroup, usernameRef) = createRefs()
        Icon(
            imageVector = Icons.Rounded.ArrowBackIosNew,
            contentDescription = "Back",
            tint = Color.White,
            modifier = Modifier
                .clickable {
                    //TODO: Handle back navigation
                    //For ripple effect
                }
                .padding(horizontal = 16.dp)
                .constrainAs(backRef) {
                    start.linkTo(parent.start)
                    end.linkTo(avatarGroup.start)
                    top.linkTo(parent.top, margin = 50.dp)
                    bottom.linkTo(parent.bottom)
                    verticalBias = 0.5f
                    width = Dimension.wrapContent
                    height = Dimension.fillToConstraints
                }
        )
        ConstraintLayout(
            modifier = Modifier
                .height(32.dp)
                .background(Color.Transparent)
                .constrainAs(avatarGroup) {
                    start.linkTo(backRef.end)
                    top.linkTo(parent.top, margin = 50.dp)
                    end.linkTo(usernameRef.start)
                    bottom.linkTo(parent.bottom)
                    verticalBias = 0.5f
                    width = Dimension.wrapContent
                    height = Dimension.fillToConstraints
                },
        )
        {
            val (avatarRef, activeRef) = createRefs()
            Image(
                painter = avatar,
                contentDescription = "Avatar",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .height(48.dp)
                    .width(48.dp)
                    .background(Color.Transparent)
                    .constrainAs(avatarRef) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                        verticalBias = 0.5f
                        horizontalBias = 0f
                    },
            )
            Box(
                modifier = Modifier
                    .background(primaryGray, CircleShape)
                    .constrainAs(activeRef) {
                        end.linkTo(avatarRef.end, margin = 5.dp)
                        bottom.linkTo(
                            avatarRef.bottom,
                            margin = 3.dp
                        )
                    },
            ) {
                Icon(
                    imageVector = Icons.Rounded.Adjust,
                    contentDescription = "Active Status",
                    tint = primaryGrayBright,
                    modifier = Modifier
                        .padding(4.dp)
                        .size(10.dp)
                        .align(Alignment.Center)
                )
            }
        }
        Text(
            text = "Username",
            color = Color.White,
            fontSize = 16.sp,
            fontFamily = ggsans,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .constrainAs(usernameRef) {
                    start.linkTo(avatarGroup.end)
                    top.linkTo(parent.top, margin = 50.dp)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                    verticalBias = 0.5f
                    width = Dimension.fillToConstraints
                    height = Dimension.wrapContent
                }
                .padding(horizontal = 8.dp, vertical = 8.dp)
        )

    }
}

//@Preview(
//    name = "Chat Body Info",
//    showBackground = true,
//)
@Composable
fun BodyInfo() {
    val primaryGray = colorResource(id = R.color.primary_gray)
    val avatar = painterResource(id = R.drawable.account)

    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(primaryGray)
            .padding(vertical = 16.dp)
    ) {
        val startGuideline = createGuidelineFromStart(8.dp)
        val endGuideline = createGuidelineFromEnd(0.dp)
        val (avatarRef, usernameRef, infoRef, introRef) = createRefs()
        Image(
            painter = avatar,
            contentDescription = "Avatar",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .height(84.dp)
                .width(84.dp)
                .background(Color.Transparent)
                .constrainAs(avatarRef) {
                    start.linkTo(startGuideline)
                    top.linkTo(parent.top)
                    end.linkTo(endGuideline)
                    bottom.linkTo(parent.bottom)
                    verticalBias = 0f
                    horizontalBias = 0f
                },
        )
        Text(
            text = "Username",
            color = Color.White,
            fontSize = 32.sp,
            fontFamily = ggsans,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.constrainAs(usernameRef) {
                start.linkTo(startGuideline)
                top.linkTo(avatarRef.bottom, margin = 8.dp)
                end.linkTo(endGuideline)
                width = Dimension.wrapContent
                height = Dimension.wrapContent
                verticalBias = 0f
                horizontalBias = 0f
            }
        )
        Text(
            text = "Username",
            color = Color.White,
            fontSize = 20.sp,
            fontFamily = ggsans,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.constrainAs(infoRef) {
                start.linkTo(startGuideline)
                top.linkTo(usernameRef.bottom, margin = 8.dp)
                end.linkTo(endGuideline)
                width = Dimension.wrapContent
                height = Dimension.wrapContent
                alpha = 0.7f
                verticalBias = 0f
                horizontalBias = 0f
            }
        )
        Text(
            text = "This is the very beginning of your legendary conversation with Username.",
            color = Color.White,
            fontSize = 16.sp,
            fontFamily = ggsans,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.constrainAs(introRef) {
                start.linkTo(startGuideline)
                top.linkTo(infoRef.bottom, margin = 8.dp)
                end.linkTo(endGuideline)
                width = Dimension.percent(0.95f)
                height = Dimension.wrapContent
                alpha = 0.5f
                verticalBias = 0f
                horizontalBias = 0f
            }
        )
    }
}

@Composable
fun LinkPreview(
    title: String,
    description: String,
    image: Painter,
    url: String = "https://www.example.com",
){
    val context = LocalContext.current
    ConstraintLayout(
        modifier = Modifier.fillMaxWidth()
            .wrapContentHeight()
            .background(Color.Transparent)
            .padding(8.dp)
            .shadow(4.dp, CircleShape)
            .clickable {
                openLinkInBrowser(
                    context = context,
                    url = url
                )
            }
    ) {
        val (imageRef) = createRefs()
        Image(
            painter = image,
            contentDescription = "Link Preview Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .height(48.dp)
                .width(48.dp)
                .background(Color.Transparent)
                .constrainAs(imageRef){
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    verticalBias = .5f
                }
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(Color.Transparent)
                .padding(start = 8.dp)
                .constrainAs(createRef()){
                    start.linkTo(imageRef.end, margin = 8.dp)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                    height = Dimension.wrapContent
                }
        ){
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontFamily = ggsans,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = description,
                color = Color.White,
                fontSize = 12.sp,
                fontFamily = ggsans,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun MessageItem(
    message: Message,
    avatar: Painter,
    images: List<Painter> = emptyList(),
) {
    val primaryGray = colorResource(id = R.color.primary_gray)
    var zoneDT = "" // if SDK is 26+ then we can get the timezone from the message
    // else blank
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // if message span is more than 1 year
        val customFormat = LocalDateTime.Format {
            hour()
            char(':')
            minute()
            char(':')
            second()
            char(' ')
            dayOfMonth()
            char('-')
            monthNumber()
            char('-')
            year()
        }
        val targetTimeZone = TimeZone.currentSystemDefault()
        val sourceTimeZone = TimeZone.of(message.sentTime.timezone)
        val localDateTimeSource = LocalDateTime.parse(message.sentTime.dateTime, customFormat)
        val instantSource = localDateTimeSource.toInstant(sourceTimeZone)
        val localDateTimeTarget = instantSource.toLocalDateTime(targetTimeZone)
        val instantTarget = localDateTimeTarget.toInstant(targetTimeZone)
        if (instantTarget.until(Clock.System.now(), DateTimeUnit.MONTH, targetTimeZone) > 12) {
            // if message span is more than 1 year, then show the date
            val DoW = localDateTimeTarget.dayOfWeek.toString().slice(0..2)
            zoneDT = "$DoW, %02d:%02d:%02d %02d-%02d-%04d".format(
                localDateTimeTarget.second,
                localDateTimeTarget.minute,
                localDateTimeTarget.hour,
                localDateTimeTarget.dayOfMonth,
                localDateTimeTarget.monthNumber,
                localDateTimeTarget.year
            )
        } else if (instantTarget.until(
                Clock.System.now(),
                DateTimeUnit.MONTH,
                targetTimeZone
            ) >= 1
        ) {
            val differences =
                instantTarget.until(Clock.System.now(), DateTimeUnit.MONTH, targetTimeZone)
            zoneDT = if (differences == 1L) {
                "%d month ago".format(differences)
            } else {
                "%d months ago".format(differences)
            }
        } else if (instantTarget.until(Clock.System.now(), DateTimeUnit.DAY, targetTimeZone) >= 1) {
            val differences =
                instantTarget.until(Clock.System.now(), DateTimeUnit.DAY, targetTimeZone)
            zoneDT = if (differences == 1L) {
                "%d day ago".format(differences)
            } else {
                "%d days ago".format(differences)
            }
        } else if (instantTarget.until(
                Clock.System.now(),
                DateTimeUnit.HOUR,
                targetTimeZone
            ) >= 1
        ) {
            val differences =
                instantTarget.until(Clock.System.now(), DateTimeUnit.HOUR, targetTimeZone)
            zoneDT = if (differences == 1L) {
                "%d hour ago".format(differences)
            } else {
                "%d hours ago".format(differences)
            }
        } else if (instantTarget.until(
                Clock.System.now(),
                DateTimeUnit.MINUTE,
                targetTimeZone
            ) >= 1
        ) {
            val differences =
                instantTarget.until(Clock.System.now(), DateTimeUnit.MINUTE, targetTimeZone)
            zoneDT = if (differences == 1L) {
                "%d minute ago".format(differences)
            } else {
                "%d minutes ago".format(differences)
            }
        } else if (instantTarget.until(
                Clock.System.now(),
                DateTimeUnit.SECOND,
                targetTimeZone
            ) >= 1
        ) {
            val differences =
                instantTarget.until(Clock.System.now(), DateTimeUnit.SECOND, targetTimeZone)
            zoneDT = if (differences == 1L) {
                "%d second ago".format(differences)
            } else {
                "%d seconds ago".format(differences)
            }
        } else if (instantTarget.until(
                Clock.System.now(),
                DateTimeUnit.SECOND,
                targetTimeZone
            ) == 0L
        ) {
            zoneDT = "Just now"
        } else { // exactly 12 months
            zoneDT = "1 year ago"
        }
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(primaryGray)
            .padding(horizontal = 8.dp, vertical = 16.dp)
    ) {
        Image(
            painter = avatar,
            contentDescription = "Avatar",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .height(36.dp)
                .width(36.dp)
                .align(Alignment.Top)
                .background(Color.Transparent)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(Color.Transparent)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = message.senderName,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontFamily = ggsans,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .wrapContentHeight()
                        .background(Color.Transparent)
                        .weight(1f)
                )
                Text(
                    text = zoneDT,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontFamily = ggsans,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .wrapContentHeight()
                        .background(Color.Transparent)
                        .alpha(0.5f)
                )
            }
            Text(
                text = message.text,
                color = Color.White,
                fontSize = 16.sp,
                fontFamily = ggsans,
                fontWeight = FontWeight.Normal,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .wrapContentHeight()
                    .background(Color.Transparent)
            )
            if(message.isImage && images.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .background(Color.Transparent)
                        .padding(start = 8.dp, top = 8.dp)
                ) {
                    for (image in images) {
                        Image(
                            painter = image,
                            contentDescription = "Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(128.dp)
                                .height((128 / image.intrinsicSize.let {
                                    if (it.height > 0) it.width / it.height else 1f
                                }).dp)
                                .padding(end = 4.dp)
                                .background(Color.Transparent)
                        )
                    }
                }
            }
            if(message.isLink && message.text.isNotEmpty()) {
                val extractedLink = extractFirstLink(message.text)
                val validLink = if(
                    extractedLink.startsWith("http://") ||
                    extractedLink.startsWith("https://") ||
                    extractedLink.startsWith("ftp://")
                ) {
                    extractedLink
                } else {
                    "https://$extractedLink"
                }
                var title by remember { mutableStateOf("") }
                var description by remember { mutableStateOf("") }
                var imageUrl by remember { mutableStateOf("") }
                var isLoading by remember { mutableStateOf(true) }

                LaunchedEffect(validLink) {
                    withContext(Dispatchers.IO) {
                        try {
                            val doc: Document = Jsoup.connect(validLink).get()
                            title = doc.title()
                            description = doc.select("meta[name=description]").attr("content")
                            imageUrl = doc.select("meta[property=og:image]").attr("content")
                            isLoading = false
                        } catch (e: Exception) {
                            // Handle connection errors
                            isLoading = false
                            Log.d("Error", "Failed to fetch link preview: ${e.message}")
                        }
                    }
                }

                if (!isLoading && title.isNotEmpty()) {
                    val image = if (imageUrl.isNotEmpty()) {
                        rememberAsyncImagePainter(
                            model = imageUrl,
                            placeholder = painterResource(id = R.drawable.account),
                            error = painterResource(id = R.drawable.account)
                        )
                    } else {
                        painterResource(id = R.drawable.account)
                    }
                    LinkPreview(title = title, description = description, image = image
                        , url = validLink)
                }
            }
        }
    }
}

@Preview(
    name = "Chat Messages",
    showBackground = true,
)
@Composable
fun BodyMessage() {
    val primaryGray = colorResource(id = R.color.primary_gray)
    val receiverAvt = painterResource(id = R.drawable.account)
    // we assume receiver is the other user, the current user is the sender
    val mockMessages = listOf(
        Message("Hello, how are you?", true),
        Message("I'm good, thanks! How about you?", false, senderName = "Receiver"),
        Message("I'm doing well too!", true),
        Message("That's great to hear!", false, senderName = "Receiver"),
        Message("What are you up to?", true),
        Message("Just working on some project.", false, senderName = "Receiver"),
        Message("Sounds interesting! What kind of project?", true),
        Message("I would like to join if you need help.", true),
        Message(
            "Sure! I could use some help with a coding project.",
            false,
            senderName = "Receiver"
        ),
        Message("Awesome! Let's work on it together.", true),
        Message("Looking forward to it!", false, senderName = "Receiver"),
        Message(
            "But it is very arduous and tedious, are you sure you want to join?",
            false,
            senderName = "Receiver"
        ),
        Message("This could take up to 6 months.", false, senderName = "Receiver"),
        Message("I am sure, I have a lot of free time.", true),
        Message("Besides, I need some projects to add to my portfolio.", true),
        Message("And highlight my skills.", true),
        Message("Okay, let's do it!", false, senderName = "Receiver"),
        Message("I will send you the details later.", false, senderName = "Receiver"),
        Message(":applaud:", true), // Emoji message, integrate later
        Message(
            "I have some images to share with you.",
            true,
            isImage = true,
            imagesList = listOf(
                painterResource(id = R.drawable.account),
                painterResource(id = R.drawable.account),
                painterResource(id = R.drawable.account)
            )
        ),
        Message("Here is the link to the project: www.example.com", false, isLink = true),
        )
    ConstraintLayout {
        val (messageRef) = createRefs()
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .background(primaryGray)
                .padding(vertical = 16.dp) // pad vertical to prevent edge of screen covering information
                .constrainAs(messageRef) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                    verticalBias = 0f
                    horizontalBias = 0f
                },
            content =
                {
                    items(mockMessages.size) { index ->
                        val message = mockMessages[index]
                        MessageItem(
                            message = message,
                            avatar = receiverAvt,
                            images = message.imagesList
                        )
                    }
                }
        )
    }
}

@Preview(
    name = "Chat Footer",
    showBackground = true,
)
@Composable
fun Footer() {
    // Footer comprised of fields and send button
    val primaryGray = colorResource(id = R.color.primary_gray)
    val primaryGrayLight = colorResource(id = R.color.primary_gray_light)
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .background(primaryGray),
    ) {
        val (mediaRef, fileRef, textFieldRef, sendRef) = createRefs()
        var text by remember { mutableStateOf("") }

        Icon(
            Icons.Rounded.PhotoLibrary,
            contentDescription = "Media",
            tint = Color.White,
            modifier = Modifier
                .clickable() {
                    //TODO: Handle media upload
                }
                .constrainAs(mediaRef) {
                    start.linkTo(parent.start, margin = 8.dp)
                    top.linkTo(parent.top)
                    end.linkTo(fileRef.start)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.wrapContent
                    height = Dimension.wrapContent
                }
        )

        Icon(
            Icons.Rounded.FileUpload,
            contentDescription = "File",
            tint = Color.White,
            modifier = Modifier
                .clickable() {
                    //TODO: Handle file upload
                }
                .constrainAs(fileRef) {
                    start.linkTo(mediaRef.end, margin = 8.dp)
                    top.linkTo(parent.top)
                    end.linkTo(textFieldRef.start)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.wrapContent
                    height = Dimension.wrapContent
                }
        )

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Type a message") },
            colors =
                TextFieldDefaults.colors(
                    focusedIndicatorColor = primaryGrayLight,
                    unfocusedIndicatorColor = primaryGrayLight,
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
                    text = "Say something...",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 16.sp,
                    fontFamily = ggsans,
                    fontWeight = FontWeight.Normal,
                )
            },
            modifier = Modifier
                .constrainAs(textFieldRef) {
                    start.linkTo(fileRef.end, margin = 8.dp)
                    top.linkTo(parent.top)
                    end.linkTo(sendRef.start)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.fillToConstraints
                    height = Dimension.wrapContent
                },
        )
        Icon(
            Icons.AutoMirrored.Rounded.Send,
            contentDescription = "Send",
            tint = Color.White,
            modifier = Modifier
                .clickable() {
                    //TODO: Handle send message
                }
                .constrainAs(sendRef) {
                    start.linkTo(textFieldRef.end, margin = 8.dp)
                    top.linkTo(parent.top)
                    end.linkTo(parent.end, margin = 8.dp)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.wrapContent
                    height = Dimension.wrapContent
                }
        )
    }
}