package com.arturo.carlauncher

import android.webkit.WebView
import android.Manifest
import android.app.Activity
import android.os.Bundle
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.webkit.WebView
import android.widget.*
import android.text.Editable
import android.text.TextWatcher

class MainActivity : Activity() {

    private val bg = Color.rgb(14, 15, 18)
    private val card = Color.rgb(29, 30, 35)
    private val red = Color.rgb(220, 45, 55)
    private val white = Color.WHITE
    private val gray = Color.rgb(160, 160, 165)

    private lateinit var content: FrameLayout
    private lateinit var navigation: LinearLayout

    private var currentPage = "INICIO"

    // BIBLIOTECA MUSICAL

    private var songs = listOf<Song>()
    private var currentPlaylist = listOf<Song>()
    private var currentIndex = -1

    private var musicTab = "CANCIONES"
    private var selectedFolder: String? = null

    private var musicTitleView: TextView? = null
    private var musicArtistView: TextView? = null
    private var musicStatusView: TextView? = null
    private var playPauseButton: TextView? = null
    private var musicCoverView: ImageView? = null

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = bg
        window.navigationBarColor = bg

        createMainLayout()
        showPage("INICIO")

        MusicPlayback.connect(this)
    }

    override fun onResume() {
        super.onResume()

        if (hasAudioPermission()) {
            loadMusicLibrary()
        }

        refreshHomeMusic()
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    // PERMISOS

    private fun hasAudioPermission(): Boolean {

        val permission = if (Build.VERSION.SDK_INT >= 33) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        return checkSelfPermission(permission) ==
            PackageManager.PERMISSION_GRANTED
    }

    private fun requestAudioPermission() {

        val permission = if (Build.VERSION.SDK_INT >= 33) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        requestPermissions(
            arrayOf(permission),
            100
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (requestCode == 100) {

            if (
                grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                loadMusicLibrary()
                showPage("MÚSICA")
            } else {
                Toast.makeText(
                    this,
                    "Se necesita permiso para consultar la música",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // CARGAR BIBLIOTECA

    private fun loadMusicLibrary() {

        if (!hasAudioPermission()) return

        Thread {

            val result = MusicLibrary.getSongs(this)

            runOnUiThread {

                songs = result

                if (currentPage == "MÚSICA") {
                    showPage("MÚSICA")
                }

                if (songs.isEmpty()) {
                    Toast.makeText(
                        this,
                        "No se encontraron canciones",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        }.start()
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
            setPadding(dp(4), 0, dp(8), 0)
        }

        val settingsButton = TextView(this).apply {
            text = "⚙"
            textSize = 23f
            gravity = Gravity.CENTER
            setTextColor(white)
            background = GradientDrawable().apply {
                setColor(card)
                cornerRadius = dp(10).toFloat()
                setStroke(dp(1), red)
            }
            setOnClickListener { showPage("AJUSTES") }
        }

        header.addView(
            settingsButton,
            LinearLayout.LayoutParams(dp(42), dp(38)).apply {
                setMargins(0, 0, dp(10), 0)
            }
        )

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
            "MÚSICA" -> musicScreen()
            "BLUETOOTH" -> bluetoothScreen()
            "AUTONOMÍA" -> autonomyScreen()
            "DIAGNOSTIC CAR" -> diagnosticScreen()
            "AJUSTES" -> settingsScreen()
            "MAPS" -> mapsScreen()
            else -> homeScreen()
        }

        content.addView(
            screen,
            FrameLayout.LayoutParams(-1, -1)
        )

        updateNavigation()

        if (page == "INICIO") {
            refreshHomeMusic()
        }
    }

    // BARRA INFERIOR

    private fun updateNavigation() {

        navigation.removeAllViews()

        val items = listOf(
            "⌂" to "INICIO",
            "▦" to "APPS",
            "♫" to "MÚSICA",
            "ᛒ" to "BLUETOOTH",
            "◉" to "AUTONOMÍA",
            "⚠" to "DIAGNOSTIC CAR",
            "⌖" to "MAPS"
        )

        items.forEach { item ->

            val selected = currentPage == item.second

            val button = TextView(this).apply {
                text = "${item.first}\n${item.second}"
                textSize = if (item.second == "DIAGNOSTIC CAR") 7f else 8f
                gravity = Gravity.CENTER
                setTextColor(if (selected) red else gray)

                typeface = if (selected) {
                    Typeface.DEFAULT_BOLD
                } else {
                    Typeface.DEFAULT
                }

                background = GradientDrawable().apply {
                    setColor(
                        if (selected) Color.rgb(55, 20, 25)
                        else Color.TRANSPARENT
                    )
                    cornerRadius = dp(9).toFloat()
                    if (selected) {
                        setStroke(dp(1), red)
                    }
                }
                setPadding(dp(1), 0, dp(1), 0)
                maxLines = 2

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

    // INICIO

    private fun homeScreen(): View {

        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(4), dp(8), dp(4), dp(8))
        }

        // MAPS

        val map = makeCard()

        map.addView(
            makeText("NAVEGACIÓN", 17f, red, true)
        )

        val mapPreview = WebView(this).apply {
            setBackgroundColor(Color.rgb(21, 25, 29))
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false
            webViewClient = android.webkit.WebViewClient()
            loadUrl("https://www.openstreetmap.org")
        }

        map.addView(
            mapPreview,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        map.addView(
            makeButton("ABRIR GOOGLE MAPS") {
                openMaps()
            }
        )

        // MUSICA

        val music = makeCard()

        music.addView(
            makeText("REPRODUCTOR", 17f, red, true)
        )

        musicCoverView = ImageView(this).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            setBackgroundColor(Color.rgb(75, 25, 32))
            setImageResource(android.R.drawable.ic_media_play)
        }

        music.addView(
            musicCoverView,
            LinearLayout.LayoutParams(-1, dp(95))
        )

        musicTitleView = makeText(
            "Selecciona una canción",
            14f,
            white,
            true
        )

        musicArtistView = makeText(
            "Artista desconocido",
            11f,
            gray
        )

        musicStatusView = makeText(
            "Biblioteca musical",
            10f,
            red,
            true
        )

        music.addView(musicTitleView)
        music.addView(musicArtistView)
        music.addView(musicStatusView)

        val controls = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        val previous = makeButton("⏮") {
            previousSong()
        }

        playPauseButton = makeButton("▶") {
            toggleSong()
        }

        val next = makeButton("⏭") {
            nextSong()
        }

        fun controlParams(): LinearLayout.LayoutParams {
            return LinearLayout.LayoutParams(
                0,
                dp(42),
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
            makeButton("ABRIR MI MÚSICA") {
                showPage("MÚSICA")
            }
        )

        panel.addView(
            map,
            LinearLayout.LayoutParams(0, -1, 1.5f)
        )

        panel.addView(
            View(this),
            LinearLayout.LayoutParams(dp(10), 1)
        )

        panel.addView(
            music,
            LinearLayout.LayoutParams(0, -1, 1f)
        )

        return panel
    }

    // APARTADO MUSICA

    private fun musicScreen(): View {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), dp(8), dp(8), dp(4))
        }

        root.addView(
            makeText("♫ MI MÚSICA", 21f, red, true)
        )

        if (!hasAudioPermission()) {

            root.addView(
                makeText(
                    "Necesitamos permiso para consultar tus canciones.",
                    14f,
                    gray
                )
            )

            root.addView(
                makeButton("PERMITIR ACCESO A MÚSICA") {
                    requestAudioPermission()
                }
            )

            return root
        }

        // PESTAÑAS

        val tabs = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        val songsTab = makeButton("CANCIONES") {
            musicTab = "CANCIONES"
            selectedFolder = null
            showPage("MÚSICA")
        }

        val foldersTab = makeButton("CARPETAS") {
            musicTab = "CARPETAS"
            selectedFolder = null
            showPage("MÚSICA")
        }

        if (musicTab == "CANCIONES") {
            songsTab.alpha = 1f
            foldersTab.alpha = 0.5f
        } else {
            songsTab.alpha = 0.5f
            foldersTab.alpha = 1f
        }

        tabs.addView(
            songsTab,
            LinearLayout.LayoutParams(0, dp(42), 1f)
        )

        tabs.addView(
            foldersTab,
            LinearLayout.LayoutParams(0, dp(42), 1f)
        )

        root.addView(tabs)

        // BUSCADOR

val search = EditText(this).apply {
    hint = "Buscar canción o artista..."
    setHintTextColor(gray)
    setTextColor(white)
    textSize = 14f
    isSingleLine = true
    setPadding(dp(12), 0, dp(12), 0)

    background = GradientDrawable().apply {
        setColor(card)
        cornerRadius = dp(10).toFloat()
    }
}

        root.addView(
            search,
            LinearLayout.LayoutParams(-1, dp(45)).apply {
                setMargins(0, dp(7), 0, dp(7))
            }
        )

        val scroll = ScrollView(this)

        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        scroll.addView(list)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        if (songs.isEmpty()) {

            list.addView(
                makeText(
                    "No se encontraron canciones.",
                    14f,
                    gray
                )
            )

            list.addView(
                makeButton("ACTUALIZAR BIBLIOTECA") {
                    loadMusicLibrary()
                }
            )

            return root
        }

        // FUNCION PARA DIBUJAR LISTA

        fun drawList(query: String = "") {

            list.removeAllViews()

            val normalizedQuery = query.trim().lowercase()

            if (musicTab == "CARPETAS" && selectedFolder == null) {

                val folders = songs
                    .groupBy { it.folder }
                    .toSortedMap(
                        compareBy(String.CASE_INSENSITIVE_ORDER) { it }
                    )

                folders.forEach { (folder, folderSongs) ->

                    val row = makeMusicRow(
                        "📁 $folder",
                        "${folderSongs.size} canciones",
                        "›"
                    ) {
                        selectedFolder = folder
                        musicTab = "CANCIONES"
                        showPage("MÚSICA")
                    }

                    list.addView(row)
                }

                return
            }

            val filtered = songs
                .filter { song ->

                    val matchesFolder =
                        selectedFolder == null ||
                        song.folder == selectedFolder

                    val matchesSearch =
                        normalizedQuery.isEmpty() ||
                        song.title.lowercase().contains(normalizedQuery) ||
                        song.artist.lowercase().contains(normalizedQuery)

                    matchesFolder && matchesSearch
                }
                .sortedBy { it.title.lowercase() }

            if (selectedFolder != null) {

                list.addView(
                    makeButton("← VOLVER A CARPETAS") {
                        selectedFolder = null
                        musicTab = "CARPETAS"
                        showPage("MÚSICA")
                    }
                )

                list.addView(
                    makeText(
                        selectedFolder ?: "",
                        13f,
                        red,
                        true
                    )
                )
            }

            list.addView(
                makeText(
                    "${filtered.size} canciones",
                    11f,
                    gray
                )
            )

            filtered.forEach { song ->

                val index = filtered.indexOf(song)

                list.addView(
                    makeMusicRow(
                        song.title,
                        song.artist,
                        "▶"
                    ) {
                        currentPlaylist = filtered
                        currentIndex = index

                        MusicPlayback.playSongs(
                            this@MainActivity,
                            currentPlaylist,
                            currentIndex
                        )

                        refreshHomeMusic()

                        Toast.makeText(
                            this@MainActivity,
                            "Reproduciendo: ${song.title}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
        }

        drawList()

        search.addTextChangedListener(
            object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    drawList(s?.toString() ?: "")
                }

                override fun afterTextChanged(
                    s: Editable?
                ) {}
            }
        )

        return root
    }

    // FILA DE CANCION O CARPETA

    private fun makeMusicRow(
        title: String,
        subtitle: String,
        actionLabel: String,
        action: () -> Unit
    ): LinearLayout {

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), dp(6), dp(8), dp(6))

            background = GradientDrawable().apply {
                setColor(card)
                cornerRadius = dp(10).toFloat()
            }

            setOnClickListener {
                action()
            }
        }

        val icon = TextView(this).apply {
            text = "♫"
            textSize = 22f
            gravity = Gravity.CENTER
            setTextColor(red)
        }

        row.addView(
            icon,
            LinearLayout.LayoutParams(dp(45), dp(45))
        )

        val labels = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
        }

        labels.addView(
            makeText(title, 13f, white, true)
        )

        labels.addView(
            makeText(subtitle, 10f, gray)
        )

        row.addView(
            labels,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        row.addView(
            makeText(actionLabel, 20f, red, true)
        )

        row.layoutParams = LinearLayout.LayoutParams(
            -1,
            dp(58)
        ).apply {
            setMargins(0, dp(3), 0, dp(3))
        }

        return row
    }

    // CONTROLES MUSICALES

    private fun toggleSong() {

        MusicPlayback.connect(this) {

            MusicPlayback.toggle()

            handler.postDelayed({
                refreshHomeMusic()
            }, 400)
        }
    }

    private fun nextSong() {

        if (currentPlaylist.isEmpty()) return

        if (currentIndex < currentPlaylist.lastIndex) {
            currentIndex++
        } else {
            currentIndex = 0
        }

        MusicPlayback.playSongs(
            this,
            currentPlaylist,
            currentIndex
        )

        handler.postDelayed({
            refreshHomeMusic()
        }, 400)
    }

    private fun previousSong() {

        if (currentPlaylist.isEmpty()) return

        if (currentIndex > 0) {
            currentIndex--
        } else {
            currentIndex = currentPlaylist.lastIndex
        }

        MusicPlayback.playSongs(
            this,
            currentPlaylist,
            currentIndex
        )

        handler.postDelayed({
            refreshHomeMusic()
        }, 400)
    }

    // ACTUALIZAR TARJETA PRINCIPAL

    private fun refreshHomeMusic() {

        if (currentPage != "INICIO") return

        val title = MusicPlayback.currentTitle()
        val artist = MusicPlayback.currentArtist()
        val playing = MusicPlayback.isPlaying()

        musicTitleView?.text = title
        musicArtistView?.text = artist

        musicStatusView?.text = if (playing) {
            "REPRODUCIENDO"
        } else {
            "EN PAUSA"
        }

        playPauseButton?.text = if (playing) "Ⅱ" else "▶"

        currentPlaylist.getOrNull(currentIndex)?.let {
            loadArtwork(it.uri)
        }
    }

    // CARATULA DEL ALBUM

    private fun loadArtwork(uri: Uri) {

        try {

            val retriever = MediaMetadataRetriever()

            retriever.setDataSource(this, uri)

            val artwork = retriever.embeddedPicture

            if (artwork != null) {

                val bitmap = BitmapFactory.decodeByteArray(
                    artwork,
                    0,
                    artwork.size
                )

                musicCoverView?.setImageBitmap(bitmap)

            } else {

                musicCoverView?.setImageResource(
                    android.R.drawable.ic_media_play
                )
            }

            retriever.release()

        } catch (e: Exception) {

            musicCoverView?.setImageResource(
                android.R.drawable.ic_media_play
            )
        }
    }

    // APLICACIONES

    private fun appsScreen(): View {

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
                        LinearLayout.LayoutParams(dp(42), dp(42))
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
                    0,
                    dp(105),
                    1f
                )

                params.setMargins(dp(4), dp(4), dp(4), dp(4))

                row.addView(tile, params)
            }

            repeat(columns - group.size) {
                row.addView(
                    View(this),
                    LinearLayout.LayoutParams(0, dp(105), 1f)
                )
            }

            column.addView(row)
        }

        scroll.addView(column)
        return scroll
    }

    // AUTONOMIA

    private fun autonomyScreen(): View {

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
                    Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
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
            0,
            -1,
            1f
        )

        params.setMargins(dp(3), dp(8), dp(3), dp(8))

        parent.addView(metric, params)
    }

    // BLUETOOTH: BASE VISUAL (CONEXION REAL EN UNA ETAPA POSTERIOR)

    private fun bluetoothScreen(): View {

        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(10), dp(12), dp(8))
        }

        column.addView(makeText("BLUETOOTH AUDIO", 21f, red, true))
        column.addView(makeText("Conecta tu celular y reproduce música desde tus aplicaciones favoritas", 12f, gray))

        val device = makeCard().apply {
            gravity = Gravity.CENTER
            setPadding(dp(18), dp(18), dp(18), dp(18))
        }

        device.addView(makeText("ᛒ", 42f, red, true))
        device.addView(makeText("TELÉFONO", 16f, white, true))
        device.addView(makeText("Sin dispositivo conectado", 12f, gray))
        device.addView(makeText("AUDIO BLUETOOTH · A2DP", 10f, red, true))

        column.addView(device, LinearLayout.LayoutParams(-1, dp(190)).apply {
            setMargins(0, dp(12), 0, dp(10))
        })

        val controls = makeCard()
        controls.addView(makeText("CONTROLES MULTIMEDIA", 15f, red, true))
        controls.addView(makeText("Anterior          ▶ / Ⅱ          Siguiente", 17f, white, true))
        controls.addView(makeText("El nombre de la canción y el artista aparecerán cuando el teléfono comparta esos datos.", 11f, gray))
        column.addView(controls, LinearLayout.LayoutParams(-1, dp(115)).apply {
            setMargins(0, 0, 0, dp(10))
        })

        column.addView(makeButton("ABRIR AJUSTES DE BLUETOOTH") {
            try {
                startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
            } catch (e: Exception) {
                Toast.makeText(this, "No se pudieron abrir los ajustes Bluetooth", Toast.LENGTH_SHORT).show()
            }
        })

        return column
    }

    // DIAGNOSTIC CAR: BASE VISUAL CON DATOS DE EJEMPLO VACÍOS

    private fun diagnosticScreen(): View {

        val scroll = ScrollView(this)
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(10), dp(12), dp(10))
        }

        column.addView(makeText("DIAGNOSTIC CAR", 22f, red, true))
        column.addView(makeText("Centro de diagnóstico del vehículo · OBD2 ELM327", 12f, gray))

        val status = makeCard().apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val engineIcon = makeText("⚠", 38f, red, true)
        status.addView(engineIcon, LinearLayout.LayoutParams(dp(64), dp(70)))
        val statusText = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(makeText("CHECK ENGINE", 16f, white, true))
            addView(makeText("Esperando conexión con ELM327", 11f, gray))
            addView(makeText("ESTADO: SIN CONEXIÓN", 10f, red, true))
        }
        status.addView(statusText, LinearLayout.LayoutParams(0, -2, 1f))
        column.addView(status, LinearLayout.LayoutParams(-1, dp(100)).apply {
            setMargins(0, dp(12), 0, dp(10))
        })

        column.addView(makeText("DATOS EN VIVO", 15f, red, true))
        val metrics = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        addMetric(metrics, "RPM", "--")
        addMetric(metrics, "VELOCIDAD", "--")
        addMetric(metrics, "REFRIGERANTE", "--")
        column.addView(metrics, LinearLayout.LayoutParams(-1, dp(112)))

        val codes = makeCard()
        codes.addView(makeText("CÓDIGOS DE FALLA", 16f, red, true))
        codes.addView(makeText("No hay códigos disponibles", 14f, white, true))
        codes.addView(makeText("Conecta el adaptador OBD2 para consultar los códigos que reporte el vehículo.", 11f, gray))
        column.addView(codes, LinearLayout.LayoutParams(-1, dp(125)).apply {
            setMargins(0, dp(8), 0, dp(10))
        })

        column.addView(makeButton("CONFIGURAR BLUETOOTH") {
            try {
                startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
            } catch (e: Exception) {
                Toast.makeText(this, "No se pudieron abrir los ajustes Bluetooth", Toast.LENGTH_SHORT).show()
            }
        })

        scroll.addView(column)
        return scroll
    }

    // AJUSTES: BASE VISUAL DE PERSONALIZACIÓN

    private fun settingsScreen(): View {

        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(10), dp(12), dp(8))
        }

        column.addView(makeText("AJUSTES", 22f, red, true))
        column.addView(makeText("Personaliza la apariencia de Arturo Car Launcher", 12f, gray))

        val vehicle = makeCard()
        vehicle.addView(makeText("MI VEHÍCULO", 16f, red, true))
        vehicle.addView(makeText("Logotipo: no seleccionado", 13f, white))
        vehicle.addView(makeText("Marca y modelo: personalizables", 11f, gray))
        vehicle.addView(makeButton("PERSONALIZAR VEHÍCULO") {
            Toast.makeText(this, "La personalización se habilitará en la siguiente etapa", Toast.LENGTH_SHORT).show()
        })
        column.addView(vehicle, LinearLayout.LayoutParams(-1, dp(150)).apply {
            setMargins(0, dp(12), 0, dp(10))
        })

        val appearance = makeCard()
        appearance.addView(makeText("APARIENCIA", 16f, red, true))
        appearance.addView(makeText("Tema: Negro / Rojo", 13f, white))
        appearance.addView(makeText("Orientación: Horizontal", 11f, gray))
        appearance.addView(makeText("Resolución de referencia: 1280 × 720", 11f, gray))
        column.addView(appearance, LinearLayout.LayoutParams(-1, dp(125)))

        return column
    }

    // MAPS

    private fun mapsScreen(): View {

        // La opción MAPS abre Google Maps a pantalla completa.
        openMaps()

        val message = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(20), dp(20), dp(20), dp(20))
            addView(makeText("GOOGLE MAPS", 22f, red, true))
            addView(makeText("Si no se abrió automáticamente, toca el botón.", 12f, gray))
            addView(makeButton("ABRIR GOOGLE MAPS") {
                openMaps()
            })
        }

        return message
    }

    // ABRIR MAPS

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
