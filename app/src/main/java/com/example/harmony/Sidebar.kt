package com.example.harmony

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import kotlin.jvm.Throws

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Sidebar.newInstance] factory method to
 * create an instance of this fragment.
 */
class Sidebar : Fragment() {
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

    @SuppressLint("ClickableViewAccessibility", "UseCompatLoadingForDrawables")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_sidebar, container, false)
        val homeBtn = view.findViewById<ConstraintLayout>(R.id.homeBtn)
        val newChannelBtn = view.findViewById<ImageView>(R.id.new_channel)
        val indicator = homeBtn.findViewById<ImageView>(R.id.selectedIndicator)
        homeBtn.setOnClickListener {
            indicator.visibility = View.VISIBLE
            indicator.bringToFront()
            if (homeBtn.id == R.id.homeBtn) {
                val focused = resources.getDrawable(R.drawable.custom_button_sidebar_focus)
                val buttonImage = homeBtn.findViewById<ImageView>(R.id.imageButton)
                buttonImage.setImageDrawable(focused)
            }
        }
        val focused = resources.getDrawable(R.drawable.add_server_focus)
        val static = resources.getDrawable(R.drawable.add_server)
//        newChannelBtn.setOnTouchListener { _, event ->
//            when (event.action) {
//                MotionEvent.ACTION_DOWN -> {
//                    (newChannelBtn as ImageView).setImageDrawable(focused)
//                    true
//                }
//                MotionEvent.ACTION_UP -> {
//                    (newChannelBtn as ImageView).setImageDrawable(static)
//                    true
//                }
//                else -> false
//            }
//        }

        newChannelBtn.setOnClickListener {
            try {
                val context = requireContext()
                val intent = Intent(context, ServerCreation::class.java)
                startActivity(intent)
            } catch (t: Throwable) {
                Log.d("Sidebar", t.toString())
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
         * @return A new instance of fragment Sidebar.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Sidebar().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}