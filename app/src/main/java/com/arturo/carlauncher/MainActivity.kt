import android.view.KeyEvent
package com.arturo.carlauncher

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.content.ComponentName
import android.net.Uri
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.*
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState

class MainActivity : Activity() {

    private val bg = Color.rgb(14, 15, 18)
    private val card = Color.rgb(29, 30, 35)
    private val red = Color.rgb(220, 45, 55)
    private val white = Color.WHITE
    private val gray = Color.rgb(160, 160, 165)

    private lateinit var content: FrameLayout
    private lateinit var navigation: LinearLayout

    private var currentPage = "INICIO"

    // YOUTUBE MUSIC
    private var musicController: MediaController? = null
    private var musicCallback: MediaController.Callback? = null

    private var musicTitleView: TextView? = null
    private var musicArtistView: TextView? = null
    private var musicStatusView: TextView? = null
    private var playPauseButton: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = bg
        window.navigationBarColor = bg

        createMainLayout()
        showPage("INICIO")
    }

    override fun onResume() {
        super.onResume()
        refreshMusicSession()
    }

    override fun onDestroy() {
        unregisterMusicCallback()
        super.onDestroy()
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    // ESTRUCTURA PRINCIPAL

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

    // CAMBIO DE PANTALLAS

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

        if (page == "INICIO") {
            refreshMusicSession()
        }
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

            if (bold) {
                typeface = Typeface.DEFAULT_BOLD
            }
        }
    }

    private fun makeButton(
        label: String,
        action: () -> Unit
    ): TextView {

        return TextView(this).apply {
            text = label
            textSize = 12f
            gravity = Gravity.CENTER
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD

            background = GradientDrawable().apply {
                setColor(red)
                cornerRadius = dp(10).toFloat()
            }

            setPadding(dp(8), dp(12), dp(8), dp(12))

            setOnClickListener {
                action()
            }
        }
    }

    // PANTALLA INICIO

    private fun homeScreen(): android.view.View {

        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(4), dp(8), dp(4), dp(8))
        }

        // TARJETA MAPS

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
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        map.addView(
            makeButton("ABRIR NAVEGACIÓN") {
                openMaps()
            }
        )

        // TARJETA MUSICA

        val music = makeCard()

        music.addView(
            makeText("YOUTUBE MUSIC", 17f, red, true)
        )

        val cover = TextView(this).apply {
            text = "♫"
            textSize = 38f
            gravity = Gravity.CENTER
            setTextColor(white)

            background = GradientDrawable().apply {
                setColor(Color.rgb(75, 25, 32))
                cornerRadius = dp(12).toFloat()
            }
        }

        music.addView(
            cover,
            LinearLayout.LayoutParams(-1, dp(55))
        )

        musicTitleView = makeText(
            "Esperando canción...",
            15f,
            white,
            true
        )

        musicArtistView = makeText(
            "Artista desconocido",
            11f,
            gray
        )

        musicStatusView = makeText(
            "Sin conexión multimedia",
            10f,
            red,
            true
        )

        music.addView(musicTitleView)
        music.addView(musicArtistView)
        music.addView(musicStatusView)

        // CONTROLES MUSICALES

        val controls = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        val previous = makeButton("⏮") {
            previousMusic()
        }

        playPauseButton = makeButton("▶") {
            toggleMusic()
        }

        val next = makeButton("⏭") {
            nextMusic()
        }

        fun controlParams(): LinearLayout.LayoutParams {
            return LinearLayout.LayoutParams(
                0,
                dp(44),
                1f
            ).apply {
                setMargins(dp(2), dp(5), dp(2), dp(5))
            }
        }

        controls.addView(previous, controlParams())
        controls.addView(playPauseButton, controlParams())
        controls.addView(next, controlParams())

        music.addView(controls)

        music.addView(
            makeButton("ABRIR YOUTUBE MUSIC") {
                openMusic()
            }
        )

        // DISTRIBUCION HORIZONTAL

        panel.addView(
            map,
            LinearLayout.LayoutParams(0, -1, 1.5f)
        )

        panel.addView(
            android.view.View(this),
            LinearLayout.LayoutParams(dp(10), 1)
        )

        panel.addView(
            music,
            LinearLayout.LayoutParams(0, -1, 1f)
        )

        return panel
    }

    // CONEXION CON YOUTUBE MUSIC

    private fun unregisterMusicCallback() {

        musicController?.let { controller ->
            musicCallback?.let { callback ->
                try {
                    controller.unregisterCallback(callback)
                } catch (_: Exception) {
                }
            }
        }

        musicCallback = null
        musicController = null
    }

    private fun refreshMusicSession() {

        try {

            val manager = getSystemService(
                MEDIA_SESSION_SERVICE
            ) as MediaSessionManager

            val component = ComponentName(
                this,
                MusicSessionListener::class.java
            )

            val controller = manager
                .getActiveSessions(component)
                .firstOrNull {
                    it.packageName ==
                        "com.google.android.apps.youtube.music"
                }

            if (controller == null) {

                unregisterMusicCallback()

                musicTitleView?.text = "YouTube Music"
                musicArtistView?.text = "Sin canción activa"
                musicStatusView?.text =
                    "Abre YouTube Music y reproduce música"

                playPauseButton?.text = "▶"

                return
            }

            if (
                musicController?.sessionToken !=
                controller.sessionToken
            ) {

                unregisterMusicCallback()

                musicController = controller

                val callback =
                    object : MediaController.Callback() {

                        override fun onMetadataChanged(
                            metadata: MediaMetadata?
                        ) {
                            updateMusicInfo()
                        }

                        override fun onPlaybackStateChanged(
                            state: PlaybackState?
                        ) {
                            updateMusicInfo()
                        }

                        override fun onSessionDestroyed() {
                            runOnUiThread {
                                refreshMusicSession()
                            }
                        }
                    }

                musicCallback = callback

                controller.registerCallback(callback)
            }

            updateMusicInfo()

        } catch (e: SecurityException) {

            musicStatusView?.text =
                "Autoriza el acceso multimedia"

        } catch (e: Exception) {

            musicStatusView?.text =
                "No se pudo conectar con YouTube Music"
        }
    }

    // ACTUALIZAR INFORMACION DE LA CANCION

    private fun updateMusicInfo() {

        val controller = musicController ?: return
        val metadata = controller.metadata

        musicTitleView?.text =
            metadata?.getString(
                MediaMetadata.METADATA_KEY_TITLE
            ) ?: "Sin canción"

        musicArtistView?.text =
            metadata?.getString(
                MediaMetadata.METADATA_KEY_ARTIST
            ) ?: "Artista desconocido"

        val playing =
            controller.playbackState?.state ==
                PlaybackState.STATE_PLAYING

        musicStatusView?.text =
            if (playing) "REPRODUCIENDO" else "EN PAUSA"

        playPauseButton?.text =
            if (playing) "Ⅱ" else "▶"
    }

    // REPRODUCCION

    private fun toggleMusic() {

        val controller = musicController

        if (controller == null) {

            refreshMusicSession()

            Toast.makeText(
                this,
                "Abre YouTube Music primero",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val playing =
            controller.playbackState?.state ==
                PlaybackState.STATE_PLAYING

        if (playing) {
            controller.transportControls.pause()
        } else {
            controller.transportControls.play()
        }
    }

    private fun nextMusic() {

        if (musicController == null) {
            refreshMusicSession()
            return
        }

        musicController?.transportControls?.skipToNext()
    }

    private fun previousMusic() {

        if (musicController == null) {
            refreshMusicSession()
            return
        }

        musicController?.transportControls?.skipToPrevious()
    }

    // PERMISOS MULTIMEDIA

    private fun openNotificationSettings() {

        try {
            startActivity(
                Intent(
                    android.provider.Settings
                        .ACTION_NOTIFICATION_LISTENER_SETTINGS
                )
            )
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "No se pudieron abrir los permisos",
                Toast.LENGTH_SHORT
            ).show()
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
                it.loadLabel(packageManager)
                    .toString()
                    .lowercase()
            }

        val columns = 4

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
                            try {
                                startActivity(launch)
                            } catch (e: Exception) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "No se pudo abrir la aplicación",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }

                val params = LinearLayout.LayoutParams(
                    0, dp(105), 1f
                )

                params.setMargins(
                    dp(4), dp(4), dp(4), dp(4)
                )

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

    // PANTALLA AUTONOMIA

    private fun autonomyScreen(): android.view.View {

        val scroll = ScrollView(this)

        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), dp(10), dp(8), dp(10))
        }

        column.addView(
            makeText(
                "AUTONOMÍA DEL VEHÍCULO",
                19f,
                red,
                true
            )
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
                        android.provider.Settings
                            .ACTION_BLUETOOTH_SETTINGS
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
                    Uri.parse(url)
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
