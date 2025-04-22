package com.example.harmony

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.core.view.WindowCompat
import com.example.harmony.composes.profile.OtherUserProfile
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.harmony.composes.fragments.NavBar
import com.example.harmony.composes.fragments.Sidebar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = colorResource(id = R.color.primary_gray))
            ) {
                val (sidebar, navBar) = createRefs()
                Sidebar(
                    modifier = Modifier
                        .width(80.dp)
                        .fillMaxHeight()
                        .constrainAs(sidebar) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                        }
                        .background(colorResource(id = R.color.primary_gray))
                )
                NavBar(
                    modifier = Modifier
                        .height(100.dp)
                        .fillMaxWidth()
                        .constrainAs(navBar) {
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                )
            }
        }
//        val intent = Intent(this, PollCreation::class.java)
//        val intent = Intent(this, ServerCreation::class.java)
//        val intent = Intent(this, Search::class.java)
//        val intent = Intent(this, DirectMessage::class.java)
//        startActivity(intent)
    }
}