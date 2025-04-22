package com.example.harmony

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.harmony.composes.TextBox
import com.example.harmony.composes.ui.theme.HarmonyTheme

class PollCreation : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            HarmonyTheme(isLightMode = false) {
                Scaffold(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = innerPadding.calculateTopPadding())
                    ) {
                        PollCreationHeader()
                        PollCreationBody()
                        PollCreationFooter()
                    }
                }
            }
        }
    }
}

@Composable
fun PollCreationHeader() {
    val activity = LocalActivity.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = {
                activity?.finish()
            },
            modifier = Modifier.size(40.dp),
            contentPadding = PaddingValues(2.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = ""
            )
        }

        Text(
            text = "Tạo khảo sát",
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        )

        Button(
            onClick = {
                activity?.finish()
            },
            modifier = Modifier.size(40.dp),
            contentPadding = PaddingValues(2.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = "",
                tint = Color.Green
            )
        }
    }
}

@Composable
fun PollCreationBody() {
    var question by remember { mutableStateOf("") }
    val selections = mutableListOf<MutableState<String>>(
        remember { mutableStateOf("") },
        remember { mutableStateOf("") })
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        // question
        item {
            TextBox(
                maxLines = 1,
                modifier = Modifier.fillMaxWidth(),
                label = "Câu hỏi",
                editable = true,
                text = question,
                onValueChange = { newText ->
                    question = newText
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Lựa chọn",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.LightGray
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(selections.size) { index ->
            PollCreationSelection(
                index = index,
                onRemoveSelection = {
                    selections.removeAt(index)
                },
                text = selections[index].value,
                onValueChange = { newText ->
                    selections[index].value = newText
                }
            )
        }

        item {
//            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = {
//                    selections.add(remember { mutableStateOf("") })
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(42.dp),
                colors = ButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    disabledContainerColor = Color.DarkGray,
                    disabledContentColor = Color.White
                )
            ) {
                Text(
                    text = "Thêm lựa chọn",
                    style = TextStyle(
                        fontSize = 16.sp
                    )
                )
            }
        }
    }
}

@Composable
fun PollCreationSelection(
    index: Int,
    onRemoveSelection: () -> Unit,
    text: String,
    onValueChange: (String) -> Unit
) {
//    var selection by remember { mutableStateOf("") }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextBox(
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f),
            label = "Lựa chọn ${index + 1}",
            editable = true,
            text = text,
            onValueChange = onValueChange
        )

        Button(
            onClick = onRemoveSelection,
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.size(40.dp),
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Filled.DeleteForever,
                contentDescription = ""
            )
        }
    }
}

@Composable
fun PollCreationFooter() {

}