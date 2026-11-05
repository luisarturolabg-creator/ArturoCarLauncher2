
package com.arturo.carlauncher

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.TextClock
import android.widget.Space
import android.widget.Toast

class MainActivity : Activity() {

    private val backgroundColor = Color.rgb(15, 18, 25)
    private val accentColor = Color.rgb(54, 191, 255)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = backgroundColor
        window.navigationBarColor = backgroundColor

        createLauncher()
    }

    private fun createLauncher() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 24)
            setBackgroundColor(backgroundColor)
        }

        val header = TextView(this).apply {
            text = "ARTURO CAR LAUNCHER"
            textSize = 19f
            setTextColor(accentColor)
            typeface = Typeface.DEFAULT_BOLD
        }

        root.addView(header)

        // Espacio superior
        root.addView(
            Space(this),
            LinearLayout.LayoutParams(1, 0, 1f)
        )

        // RELOJ CENTRAL
        val clock = TextClock(this).apply {
            format24Hour = "HH:mm"
            format12Hour = "h:mm a"
            textSize = 76f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            includeFontPadding = false
            isSingleLine = true
        }

        root.addView(
            clock,
            LinearLayout.LayoutParams(-1, 125)
        )

        // FECHA
        val date = TextClock(this).apply {
            format24Hour = "EEEE, dd MMMM yyyy"
            format12Hour = "EEEE, dd MMMM yyyy"
            textSize = 18f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
        }

        root.addView(
            date,
            LinearLayout.LayoutParams(-1, 45)
        )

        // Espacio entre reloj y botones
        root.addView(
            Space(this),
            LinearLayout.LayoutParams(1, 0, 1f)
        )

        // BOTONES INFERIORES
        val buttons = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        addButton(buttons, "🗺\nMAPS") {
            openMaps()
        }

        addButton(buttons, "♫\nMUSIC") {
            openYouTubeMusic()
        }

        addButton(buttons, "ᛒ\nBLUETOOTH") {
            startActivity(
                Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            )
        }

        addButton(buttons, "⚙\nAJUSTES") {
            startActivity(
                Intent(Settings.ACTION_SETTINGS)
            )
        }

        root.addView(
            buttons,
            LinearLayout.LayoutParams(-1, 105)
        )

        setContentView(root)
    }

    private fun addButton(
        parent: LinearLayout,
        label: String,
        action: () -> Unit
    ) {

        val button = TextView(this).apply {
            text = label
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD

            background = GradientDrawable().apply {
                setColor(Color.rgb(35, 43, 55))
                cornerRadius = 20f
                setStroke(2, accentColor)
            }

            setOnClickListener {
                action()
            }
        }

        val params = LinearLayout.LayoutParams(
            0, -1, 1f
        )

        params.setMargins(8, 5, 8, 5)

        parent.addView(button, params)
    }

    // GOOGLE MAPS
    private fun openMaps() {

        val intent = packageManager
            .getLaunchIntentForPackage(
                "com.google.android.apps.maps"
            )

        if (intent != null) {
            startActivity(intent)
        } else {
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.google.com/maps")
                    )
                )
            } catch (e: Exception) {
                Toast.makeText(
                    this,
                    "No se encontró una aplicación de mapas",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // YOUTUBE MUSIC
    private fun openYouTubeMusic() {

        val packageName =
            "com.google.android.apps.youtube.music"

        val intent =
            packageManager.getLaunchIntentForPackage(packageName)

        if (intent != null) {
            startActivity(intent)
        } else {
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            "market://details?id=$packageName"
                        )
                    )
                )
            } catch (e: Exception) {
                try {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(
                                "https://play.google.com/store/apps/details?id=$packageName"
                            )
                        )
                    )
                } catch (ex: Exception) {
                    Toast.makeText(
                        this,
                        "No se pudo abrir YouTube Music",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
