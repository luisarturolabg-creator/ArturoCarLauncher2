
package com.arturo.carlauncher
import android.content.ComponentName
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.*

class MainActivity : Activity() {

    private val bg = Color.rgb(14, 15, 18)
    private val card = Color.rgb(29, 30, 35)
    private val red = Color.rgb(220, 45, 55)
    private val white = Color.WHITE
    private val gray = Color.rgb(160, 160, 165)

    private lateinit var content: FrameLayout
    private lateinit var navigation: LinearLayout

    private var currentPage = "INICIO"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = bg
        window.navigationBarColor = bg

        createMainLayout()
        showPage("INICIO")
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun createMainLayout() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
            setPadding(dp(12), dp(8), dp(12), dp(8))
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), 0, dp(8), 0)
        }

        val logo = TextView(this).apply {
            text = "ARTURO CAR"
            textSize = 20f
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD
        }

        val subtitle = TextView(this).apply {
            text = "  LAUNCHER"
            textSize = 11f
            setTextColor(red)
        }

        header.addView(logo)
        header.addView(subtitle)

        root.addView(
            header,
            LinearLayout.LayoutParams(-1, dp(48))
        )

        content = FrameLayout(this)

        root.addView(
            content,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        navigation = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.rgb(24, 24, 28))
        }

        root.addView(
            navigation,
            LinearLayout.LayoutParams(-1, dp(66))
        )

        setContentView(root)
    }

    private fun showPage(page: String) {

        currentPage = page
        content.removeAllViews()

        val screen = when (page) {
            "INICIO" -> homeScreen()
            "APPS" -> appsScreen()
            "AUTONOMÍA" -> autonomyScreen()
            "MAPS" -> mapsScreen()
            else -> homeScreen()
        }

        content.addView(
            screen,
            FrameLayout.LayoutParams(-1, -1)
        )

        updateNavigation()
    }

    // BARRA INFERIOR

    private fun updateNavigation() {

        navigation.removeAllViews()

        val items = listOf(
            "⌂" to "INICIO",
            "▦" to "APPS",
            "◉" to "AUTONOMÍA",
            "⌖" to "MAPS"
        )

        items.forEach { item ->

            val selected = currentPage == item.second

            val button = TextView(this).apply {
                text = "${item.first}\n${item.second}"
                textSize = 11f
                gravity = Gravity.CENTER
                setTextColor(if (selected) red else gray)
                typeface = if (selected)
                    Typeface.DEFAULT_BOLD
                else
                    Typeface.DEFAULT

                setBackgroundColor(
                    if (selected)
                        Color.rgb(43, 27, 30)
                    else
                        Color.TRANSPARENT
                )

                setOnClickListener {
                    showPage(item.second)
                }
            }

            navigation.addView(
                button,
                LinearLayout.LayoutParams(0, -1, 1f)
            )
        }
    }

    // COMPONENTES VISUALES

    private fun makeCard(): LinearLayout {

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(14))
            background = GradientDrawable().apply {
                setColor(card)
                cornerRadius = dp(15).toFloat()
            }
        }
    }

    private fun makeText(
        value: String,
        size: Float,
        color: Int = white,
        bold: Boolean = false
    ): TextView {

        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) typeface = Typeface.DEFAULT_BOLD
        }
    }

    private fun makeButton(
        label: String,
        action: () -> Unit
    ): TextView {

        return TextView(this).apply {
            text = label
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD

            background = GradientDrawable().apply {
                setColor(red)
                cornerRadius = dp(10).toFloat()
            }

            setPadding(dp(12), dp(12), dp(12), dp(12))

            setOnClickListener { action() }
        }
    }

    // PANTALLA INICIO

    private fun homeScreen(): android.view.View {

        val landscape =
            resources.configuration.orientation == 2

        val panel = LinearLayout(this).apply {
            orientation = if (landscape)
                LinearLayout.HORIZONTAL
            else
                LinearLayout.VERTICAL

            setPadding(dp(4), dp(8), dp(4), dp(8))
        }

        val map = makeCard()

        map.addView(
            makeText("NAVEGACIÓN", 17f, red, true)
        )

        val mapPreview = TextView(this).apply {
            text = "⌖\n\nGOOGLE MAPS\n\nVista del mapa"
            textSize = 20f
            gravity = Gravity.CENTER
            setTextColor(gray)
            background = GradientDrawable().apply {
                setColor(Color.rgb(21, 25, 29))
                cornerRadius = dp(12).toFloat()
            }
        }

        map.addView(
            mapPreview,
            LinearLayout.LayoutParams( -1, 0, 1f )
        )

        map.addView(
            makeButton("ABRIR NAVEGACIÓN") {
                openMaps()
            }
        )

        val music = makeCard()

        music.addView(
            makeText("REPRODUCTOR", 17f, red, true)
        )

        val cover = TextView(this).apply {
            text = "♫"
            textSize = 65f
            gravity = Gravity.CENTER
            setTextColor(white)
            background = GradientDrawable().apply {
                setColor(Color.rgb(75, 25, 32))
                cornerRadius = dp(12).toFloat()
            }
        }

        music.addView(
            cover,
            LinearLayout.LayoutParams(
                -1,
                if (landscape) 0 else dp(130),
                if (landscape) 1f else 0f
            )
        )

        music.addView(
            makeText("YouTube Music", 17f, white, true)
        )

        music.addView(
            makeText(
                "Canción y artista aparecerán aquí",
                12f,
                gray
            )
        )

        music.addView(
            makeButton("ABRIR MÚSICA") {
                openMusic()
            }
        )

        if (landscape) {

            panel.addView(
                map,
                LinearLayout.LayoutParams(0, -1, 1.5f)
            )

            val spacer = android.view.View(this)
            panel.addView(
                spacer,
                LinearLayout.LayoutParams(dp(10), 1)
            )

            panel.addView(
                music,
                LinearLayout.LayoutParams(0, -1, 1f)
            )

            return panel

        } else {

            val scroll = ScrollView(this)

            val column = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
            }

            column.addView(
                map,
                LinearLayout.LayoutParams(-1, dp(260))
            )

            column.addView(
                music,
                LinearLayout.LayoutParams(-1, dp(240))
            )

            scroll.addView(column)
            return scroll
        }
    }

    // PANTALLA APLICACIONES

    private fun appsScreen(): android.view.View {

        val scroll = ScrollView(this)

        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), dp(10), dp(8), dp(10))
        }

        column.addView(
            makeText("MIS APLICACIONES", 19f, red, true)
        )

        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val apps = packageManager
            .queryIntentActivities(intent, 0)
            .filter {
                it.activityInfo.packageName != packageName
            }
            .sortedBy {
                it.loadLabel(packageManager).toString().lowercase()
            }

        val landscape =
            resources.configuration.orientation == 2

        val columns = if (landscape) 4 else 3

        apps.chunked(columns).forEach { group ->

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            group.forEach { info ->

                val tile = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                    setPadding(dp(5), dp(8), dp(5), dp(8))
                    background = GradientDrawable().apply {
                        setColor(card)
                        cornerRadius = dp(12).toFloat()
                    }

                    val icon = ImageView(this@MainActivity).apply {
                        setImageDrawable(
                            info.loadIcon(packageManager)
                        )
                    }

                    addView(
                        icon,
                        LinearLayout.LayoutParams(
                            dp(42), dp(42)
                        )
                    )

                    val name = TextView(this@MainActivity).apply {
                        text = info.loadLabel(packageManager)
                        textSize = 11f
                        setTextColor(white)
                        gravity = Gravity.CENTER
                        maxLines = 2
                    }

                    addView(name)

                    setOnClickListener {

                        val launch = packageManager
                            .getLaunchIntentForPackage(
                                info.activityInfo.packageName
                            )

                        if (launch != null) {
                            startActivity(launch)
                        }
                    }
                }

                val params = LinearLayout.LayoutParams(
                    0, dp(105), 1f
                )

                params.setMargins(dp(4), dp(4), dp(4), dp(4))

                row.addView(tile, params)
            }

            repeat(columns - group.size) {
                row.addView(
                    android.view.View(this),
                    LinearLayout.LayoutParams(0, dp(105), 1f)
                )
            }

            column.addView(row)
        }

        scroll.addView(column)
        return scroll
    }

    // PANTALLA AUTONOMÍA

    private fun autonomyScreen(): android.view.View {

        val scroll = ScrollView(this)

        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), dp(10), dp(8), dp(10))
        }

        column.addView(
            makeText("AUTONOMÍA DEL VEHÍCULO", 19f, red, true)
        )

        column.addView(
            makeText(
                "Información de consumo y recorrido",
                13f,
                gray
            )
        )

        val metrics = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        addMetric(metrics, "KM/L", "--")
        addMetric(metrics, "LITROS", "--")
        addMetric(metrics, "GASTO", "$--")

        column.addView(
            metrics,
            LinearLayout.LayoutParams(-1, dp(120))
        )

        val details = makeCard()

        details.addView(
            makeText("DATOS DEL VIAJE", 16f, red, true)
        )

        details.addView(makeText("Distancia: -- km", 15f))
        details.addView(makeText("Velocidad: -- km/h", 15f))
        details.addView(makeText("Tiempo: --", 15f))
        details.addView(makeText("Combustible: -- litros", 15f))

        column.addView(
            details,
            LinearLayout.LayoutParams(-1, dp(180))
        )

        val obd = makeCard()

        obd.addView(
            makeText("CONEXIÓN OBD2", 17f, red, true)
        )

        obd.addView(
            makeText(
                "Adaptador: ELM327 Bluetooth clásico",
                14f
            )
        )

        obd.addView(
            makeText(
                "Estado: pendiente de conexión",
                13f,
                gray
            )
        )

        obd.addView(
            makeButton("CONFIGURAR BLUETOOTH") {
                startActivity(
                    Intent(
                        android.provider.Settings.ACTION_BLUETOOTH_SETTINGS
                    )
                )
            }
        )

        column.addView(
            obd,
            LinearLayout.LayoutParams(-1, dp(180))
        )

        scroll.addView(column)
        return scroll
    }

    private fun addMetric(
        parent: LinearLayout,
        title: String,
        value: String
    ) {

        val metric = makeCard()

        metric.gravity = Gravity.CENTER

        metric.addView(
            makeText(value, 23f, white, true)
        )

        metric.addView(
            makeText(title, 11f, red, true)
        )

        val params = LinearLayout.LayoutParams(
            0, -1, 1f
        )

        params.setMargins(dp(3), dp(8), dp(3), dp(8))

        parent.addView(metric, params)
    }

    // PANTALLA MAPS

    private fun mapsScreen(): android.view.View {

        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(20), dp(20), dp(20), dp(20))
        }

        column.addView(
            makeText("GOOGLE MAPS", 25f, red, true)
        )

        column.addView(
            makeText(
                "Navegación y búsqueda de destinos",
                14f,
                gray
            )
        )

        column.addView(
            makeButton("ABRIR GOOGLE MAPS") {
                openMaps()
            }
        )

        return column
    }

    // ABRIR APLICACIONES

    private fun openMaps() {

        val intent = packageManager
            .getLaunchIntentForPackage(
                "com.google.android.apps.maps"
            )

        if (intent != null) {
            startActivity(intent)
        } else {
            openWeb("https://www.google.com/maps")
        }
    }

    private fun openMusic() {

        val intent = packageManager
            .getLaunchIntentForPackage(
                "com.google.android.apps.youtube.music"
            )

        if (intent != null) {
            startActivity(intent)
        } else {
            openWeb(
                "https://play.google.com/store/apps/details?id=com.google.android.apps.youtube.music"
            )
        }
    }

    private fun openWeb(url: String) {

        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    android.net.Uri.parse(url)
                )
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
