package com.arturo.carlauncher

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.provider.Settings
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {

    private val backgroundColor = Color.rgb(15, 18, 25)
    private val accentColor = Color.rgb(0, 180, 255)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = backgroundColor
        window.navigationBarColor = backgroundColor

        createLauncher()
    }

    private fun createLauncher() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(35, 25, 35, 25)
            setBackgroundColor(backgroundColor)
        }

        val header = TextView(this).apply {
            text = "ARTURO CAR LAUNCHER"
            textSize = 18f
            setTextColor(accentColor)
            typeface = Typeface.DEFAULT_BOLD
        }

        root.addView(header)

        val clock = TextView(this).apply {
            text = SimpleDateFormat(
                "HH:mm",
                Locale.getDefault()
            ).format(Date())
            textSize = 65f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }

        root.addView(
            clock,
            LinearLayout.LayoutParams(
                -1, 110
            )
        )

        val date = TextView(this).apply {
            text = SimpleDateFormat(
                "EEEE, dd MMMM yyyy",
                Locale.getDefault()
            ).format(Date())
            textSize = 17f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
        }

        root.addView(date)

        val spacer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        root.addView(
            spacer,
            LinearLayout.LayoutParams(1, 25)
        )

        val buttons = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        addButton(buttons, "MAPS") {
            openApp("com.google.android.apps.maps")
        }

        addButton(buttons, "MUSIC") {
            openApp("com.google.android.apps.youtube.music")
        }

        addButton(buttons, "BLUETOOTH") {
            startActivity(
                Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            )
        }

        addButton(buttons, "SETTINGS") {
            startActivity(
                Intent(Settings.ACTION_SETTINGS)
            )
        }

        root.addView(buttons)

        setContentView(root)
    }

    private fun addButton(
        parent: LinearLayout,
        label: String,
        action: () -> Unit
    ) {

        val button = TextView(this).apply {
            text = label
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD

            background = GradientDrawable().apply {
                setColor(Color.rgb(35, 43, 55))
                cornerRadius = 18f
                setStroke(2, accentColor)
            }

            setOnClickListener {
                action()
            }
        }

        val params = LinearLayout.LayoutParams(
            0, 85, 1f
        )

        params.setMargins(6, 5, 6, 5)

        parent.addView(button, params)
    }

    private fun openApp(packageName: String) {

        val launchIntent =
            packageManager.getLaunchIntentForPackage(packageName)

        if (launchIntent != null) {
            startActivity(launchIntent)
        } else {
            Toast.makeText(
                this,
                "Aplicación no instalada",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
