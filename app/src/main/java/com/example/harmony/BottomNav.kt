package com.example.harmony

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.compose.setContent
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.example.harmony.composes.profile.MyProfile
import com.example.harmony.composes.ui.theme.HarmonyTheme
import com.google.android.material.bottomnavigation.BottomNavigationView

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [BottomNav.newInstance] factory method to
 * create an instance of this fragment.
 */
class BottomNav : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bottom_nav, container, false)
        view.findViewById<BottomNavigationView>(R.id.bottomNavView).setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    // Handle home button click
                    true
                }
                R.id.notifications -> {
                    // Handle notifications button click
                    true
                }
                R.id.profile -> {
                    val context = requireContext()
                    val activity = context as? ComponentActivity
                    activity?.setContent {
                        HarmonyTheme (isLightMode = false) {
                            var state by remember { mutableStateOf(false) }
                            MyProfile(
                                displayedName = "ketamean",
                                username = "_ketamean123",
                                bio = "Toi sẽ giới thiệu về bản thân mình thật là ngắn gọn nhé",
                                avatarUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQJxo2NFiYcR35GzCk5T3nxA7rGlSsXvIfJwg&s",
                                modifier = Modifier,
                                onDismissRequest = {
                                    state = false
                                    activity.setContentView(R.layout.activity_main)
                                    WindowCompat.setDecorFitsSystemWindows(activity.window, false)
                                }
                            )
                        }
                    }
                    true
                }
                else -> false
            }
        }

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment BottomNav.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BottomNav().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}