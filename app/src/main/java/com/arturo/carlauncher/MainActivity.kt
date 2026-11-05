
package com.arturo.carlauncher

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.content.res.Configuration
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.TextClock
import android.widget.Space
import android.widget.Toast

class MainActivity : Activity() {

    private val bg = Color.rgb(15, 18, 25)
    private val accent = Color.rgb(54, 191, 255)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = bg
        window.navigationBarColor = bg

        createLauncher()
    }

    private fun createLauncher() {

        val dm = resources.displayMetrics
        val width = dm.widthPixels / dm.density
        val height = dm.heightPixels / dm.density

        val landscape =
            resources.configuration.orientation ==
            Configuration.ORIENTATION_LANDSCAPE

        val padding = (width * 0.035f).coerceIn(12f, 32f)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                padding.toInt(),
                padding.toInt(),
                padding.toInt(),
                padding.toInt()
            )
            setBackgroundColor(bg)
        }

        // ENCABEZADO
        val header = TextView(this).apply {
            text = "ARTURO CAR LAUNCHER"
            textSize = (width * 0.018f).coerceIn(13f, 20f)
            setTextColor(accent)
            typeface = Typeface.DEFAULT_BOLD
        }

        root.addView(
            header,
            LinearLayout.LayoutParams(-1, -2)
        )

        // RELOJ CENTRAL
        val clock = TextClock(this).apply {
            format24Hour = "HH:mm"
            format12Hour = "h:mm a"
            textSize = (minOf(width * 0.13f, height * 0.18f))
                .coerceIn(38f, 78f)
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            includeFontPadding = false
            isSingleLine = true
        }

        root.addView(
            clock,
            LinearLayout.LayoutParams(
                -1, 0, 1f
            )
        )

        // FECHA
        val date = TextClock(this).apply {
            format24Hour = "EEEE, dd MMMM yyyy"
            format12Hour = "EEEE, dd MMMM yyyy"
            textSize = (width * 0.022f).coerceIn(12f, 19f)
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
        }

        root.addView(
            date,
            LinearLayout.LayoutParams(-1, -2)
        )

        // ESPACIO FLEXIBLE
        root.addView(
            Space(this),
            LinearLayout.LayoutParams(1, 0, 0.7f)
        )

        // BOTONES ADAPTABLES
        val buttons = listOf(
            "🗺\nMAPS" to { openMaps() },
            "♫\nMUSIC" to { openYouTubeMusic() },
            "ᛒ\nBLUETOOTH" to { openBluetooth() },
            "⚙\nAJUSTES" to { openSettings() }
        )

        if (landscape) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }

            buttons.forEach { (label, action) ->
                addButton(row, label, action)
            }

            root.addView(
                row,
                LinearLayout.LayoutParams(
                    -1,
                    (height * 0.16f).coerceIn(65f, 105f).toInt()
                )
            )

        } else {
            val firstRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            val secondRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            buttons.forEachIndexed { index, item ->
                if (index < 2) {
                    addButton(firstRow, item.first, item.second)
                } else {
                    addButton(secondRow, item.first, item.second)
                }
            }

            val buttonHeight =
                (height * 0.085f).coerceIn(55f, 85f).toInt()

            root.addView(
                firstRow,
                LinearLayout.LayoutParams(-1, buttonHeight)
            )

            root.addView(
                secondRow,
                LinearLayout.LayoutParams(-1, buttonHeight)
            )
        }

        setContentView(root)
    }

    private fun addButton(
        parent: LinearLayout,
        label: String,
        action: () -> Unit
    ) {

        val width = resources.displayMetrics.widthPixels /
            resources.displayMetrics.density

        val button = TextView(this).apply {
            text = label
            textSize = (width * 0.018f).coerceIn(12f, 17f)
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD

            background = GradientDrawable().apply {
                setColor(Color.rgb(35, 43, 55))
                cornerRadius = 18f
                setStroke(2, accent)
            }

            setOnClickListener {
                action()
            }
        }

        val params = LinearLayout.LayoutParams(
            0, -1, 1f
        )

        params.setMargins(5, 5, 5, 5)

        parent.addView(button, params)
    }

    private fun openMaps() {
        val intent = packageManager.getLaunchIntentForPackage(
            "com.google.android.apps.maps"
        )

        if (intent != null) {
            startActivity(intent)
        } else {
            openWeb("https://www.google.com/maps")
        }
    }

    private fun openYouTubeMusic() {
        val packageName =
            "com.google.android.apps.youtube.music"

        val intent =
            packageManager.getLaunchIntentForPackage(packageName)

        if (intent != null) {
            startActivity(intent)
        } else {
            openWeb(
                "https://play.google.com/store/apps/details?id=$packageName"
            )
        }
    }

    private fun openBluetooth() {
        startActivity(
            Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
        )
    }

    private fun openSettings() {
        startActivity(
            Intent(Settings.ACTION_SETTINGS)
        )
    }

    private fun openWeb(url: String) {
        try {
            startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(url))
            )
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "No se pudo abrir la aplicación",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

