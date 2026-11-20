package com.gymwatch.app

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.*
import android.text.TextUtils
import android.util.TypedValue
import android.view.*
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class MainActivity : Activity() {

    private val bgColor = Color.parseColor("#1A1A1A")
    private val deepColor = Color.parseColor("#111111")
    private val surfaceColor = Color.parseColor("#252525")
    private val surface2Color = Color.parseColor("#323232")
    private val borderColor = Color.parseColor("#3B3B3B")
    private val accentColor = Color.parseColor("#FFCC00")
    private val textColor = Color.WHITE
    private val mutedColor = Color.parseColor("#A0A0A0")
    private val dangerColor = Color.parseColor("#FF5252")

    data class MachineSpec(
        val id: String,
        val cat: String,
        val name: String,
        val shortName: String,
        val defSets: Int,
        val defReps: Int,
        val defWeight: Double,
        val defRest: Int,
        val target: String,
        val tips: String
    )

    data class ExerciseItem(
        val id: String,
        var icon: String,
        var name: String,
        var weight: Double,
        var sets: Int,
        var reps: Int,
        var rest: Int,
        var completedSets: Int = 0
    )

    data class WorkoutRoutine(
        val id: String,
        var name: String,
        val exercises: MutableList<ExerciseItem>
    )

    private val categories = listOf(
        "Schiena e spalle",
        "Petto e braccia",
        "Gambe e glutei",
        "Addome e core",
        "Cardio & Corpo libero"
    )

    private val catalog = listOf(
        MachineSpec("lat", "Schiena e spalle", "Lat Machine", "Lat Mach.", 4, 10, 45.0, 90,
            "Gran dorsale, romboidi, parte posteriore delle spalle e bicipiti.",
            "Blocca le cosce sotto i rulli. Presa ampia, busto leggermente inclinato indietro (~15°), porta la sbarra alle clavicole senza dondolare."),
        MachineSpec("pulley", "Schiena e spalle", "Pulley", "Pulley", 4, 10, 40.0, 90,
            "Centro schiena (trapezio medio, romboidi) e dorsali.",
            "Schiena dritta a 90°, ginocchia leggermente piegate. Tira la maniglia verso l'ombelico chiudendo le scapole tra loro."),
        MachineSpec("verticalrow", "Schiena e spalle", "Vertical Row", "Vert. Row", 3, 10, 35.0, 60,
            "Dorsali, trapezio e deltoidi posteriori con appoggio stabile.",
            "Petto ben appoggiato al cuscino frontale. Tira i gomiti indietro lungo i fianchi evitando di alzare le spalle."),
        MachineSpec("shoulder", "Schiena e spalle", "Shoulder Press", "Shoulder", 4, 10, 25.0, 90,
            "Deltoidi (spalle) anteriori e laterali, più tricipiti.",
            "Maniglie all'altezza delle spalle. Schiena aderente allo schienale, spingi in alto senza inarcare la zona lombare."),
        MachineSpec("delts", "Schiena e spalle", "Delts Machine", "Delts", 3, 12, 20.0, 60,
            "Deltoidi laterali per ampiezza e forma della spalla.",
            "Gomiti appoggiati ai cuscinetti laterali. Solleva fino a formare una T all'altezza delle spalle senza slanci col busto."),

        MachineSpec("chest", "Petto e braccia", "Chest Press", "Chest Pr.", 4, 10, 32.5, 60,
            "Grande pettorale, deltoidi anteriori e tricipiti.",
            "Maniglie allineate a metà petto, scapole chiuse sullo schienale. Spingi in avanti senza bloccare di scatto i gomiti."),
        MachineSpec("pectoral", "Petto e braccia", "Pectoral Machine", "Pectoral", 3, 12, 30.0, 60,
            "Isolamento diretto dei pettorali.",
            "Gomiti leggermente flessi. Chiudi davanti al petto stringendo 1 secondo, poi riapri controllando lo stiramento."),
        MachineSpec("armcurl", "Petto e braccia", "Arm Curl", "Arm Curl", 3, 12, 20.0, 60,
            "Bicipiti brachiali e flessori dell'avambraccio.",
            "Retro delle braccia ben appoggiato sul cuscino inclinato, gomiti fissi in linea col perno. Fletti verso le spalle."),
        MachineSpec("armext", "Petto e braccia", "Arm Extension", "Arm Ext.", 3, 12, 22.5, 60,
            "Tricipiti (parte posteriore del braccio).",
            "Schiena aderente, gomiti fermi accanto ai fianchi. Estendi gli avambracci verso il basso/avanti senza muovere le spalle."),

        MachineSpec("legpress", "Gambe e glutei", "Leg Press", "Leg Press", 4, 10, 100.0, 90,
            "Quadricipiti, glutei e femorali.",
            "Piedi larghezza spalle sulla pedana, bacino incollato al sedile. Spingi coi talloni senza bloccare le ginocchia in cima."),
        MachineSpec("legext", "Gambe e glutei", "Leg Extension", "Leg Ext.", 3, 12, 40.0, 60,
            "Isolamento diretto dei quadricipiti.",
            "Allinea le ginocchia al perno della macchina e il rullo sopra le caviglie. Estendi completamente e scendi lento."),
        MachineSpec("legcurl", "Gambe e glutei", "Leg Curl", "Leg Curl", 3, 12, 35.0, 60,
            "Femorali (parte posteriore della coscia).",
            "Allinea le ginocchia al perno. Porta i talloni verso i glutei evitando di sollevare il bacino."),
        MachineSpec("abductor", "Gambe e glutei", "Abductor", "Abductor", 3, 15, 40.0, 60,
            "Medio e piccolo gluteo, esterno coscia.",
            "Schiena dritta. Spingi le cosce verso l'esterno e controlla il ritorno senza far sbattere i pesi."),
        MachineSpec("adductor", "Gambe e glutei", "Adductor", "Adductor", 3, 15, 35.0, 60,
            "Adduttori (interno coscia).",
            "Imposta un'apertura comoda senza forzare le anche. Chiudi lentamente al centro stringendo 1 secondo."),
        MachineSpec("glute", "Gambe e glutei", "Glute", "Glute", 3, 12, 30.0, 60,
            "Grande gluteo (estensione dell'anca).",
            "Appoggia busto e gomiti al supporto. Spingi la pedana indietro col tallone contraendo il gluteo senza inarcare la schiena."),

        MachineSpec("abs", "Addome e core", "Abdominal Crunch", "Abs Crunch", 3, 15, 25.0, 45,
            "Retto dell'addome e core.",
            "Non tirare col collo! Espira svuotando l'aria mentre arrotoli la colonna verso le ginocchia usando solo l'addome."),
        MachineSpec("lowerback", "Addome e core", "Lower Back", "Lower Back", 3, 15, 25.0, 45,
            "Lombari ed erettori spinali.",
            "Rullo dietro le scapole, piedi bloccati. Estendi il busto indietro in modo fluido fino a 180°, senza strappi."),

        MachineSpec("bike", "Cardio & Corpo libero", "Cyclette", "Cyclette", 1, 15, 5.0, 60,
            "Cardio a basso impatto e resistenza gambe.",
            "Sella all'altezza del fianco: al punto più basso del pedale il ginocchio deve restare leggermente flesso (~25°)."),
        MachineSpec("treadmill", "Cardio & Corpo libero", "Tapis Roulant", "Tapis", 1, 20, 1.0, 60,
            "Resistenza cardiovascolare e consumo calorico.",
            "Sguardo avanti (non sui piedi!), postura alta, pendenza 1–2% per simulare la corsa/camminata su strada."),
        MachineSpec("body_upper", "Cardio & Corpo libero", "Corpo Libero (Alto)", "Lib. Alto", 3, 12, 0.0, 60,
            "Push-up, dip, trazioni o manubri per parte alta.",
            "Corpo in linea retta con addome e glutei attivi. Scendi controllato tenendo i gomiti a circa 45° dal busto."),
        MachineSpec("body_legs", "Cardio & Corpo libero", "Corpo Libero (Gambe)", "Lib. Gambe", 4, 12, 0.0, 60,
            "Squat, affondi o ponte glutei.",
            "Piedi larghezza spalle, scendi portando indietro i fianchi, schiena neutra e ginocchia in linea con le punte dei piedi."),
        MachineSpec("body_core", "Cardio & Corpo libero", "Corpo Libero (Core)", "Lib. Core", 3, 30, 0.0, 45,
            "Plank, crunch a terra, mountain climber.",
            "Nel Plank gomiti sotto le spalle, forma una linea retta testa-talloni senza far cadere i fianchi. Respira regolare.")
    )

    private val routines = mutableListOf<WorkoutRoutine>()
    private var activeRoutineId = "w1"
    private var currentExerciseId: String? = null
    private var editingExerciseId: String? = null

    private enum class Screen { HOME, LIST, FOCUS, EDITOR, GUIDE, GUIDE_DETAIL }
    private var currentScreen = Screen.HOME
    private var guideBackScreen = Screen.HOME
    private var guideFilterCat = "Tutti"
    private var activeGuideSpec: MachineSpec = catalog[0]
    private var guideOpenedFromFocus = false

    // Timer
    private var countDownTimer: CountDownTimer? = null
    private var timerState = "idle" // idle, running, paused, finished
    private var remainingMs = 60000L
    private var totalRestMs = 60000L

    private lateinit var rootContainer: FrameLayout
    private lateinit var focusRingFrame: SquareRingFrame

    // Editor draft
    private var edIcon = "chest"
    private var edName = ""
    private var edWeight = 30.0
    private var edSets = 4
    private var edReps = 10
    private var edRest = 60

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadData()
        rootContainer = FrameLayout(this).apply {
            setBackgroundColor(bgColor)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        setContentView(rootContainer)
        renderHome()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        when (currentScreen) {
            Screen.HOME -> super.onBackPressed()
            Screen.LIST -> renderHome()
            Screen.FOCUS -> {
                stopTimer()
                currentExerciseId = null
                renderList()
            }
            Screen.EDITOR -> {
                val cur = currentExerciseId
                if (cur != null && editingExerciseId == cur) enterFocus(cur) else renderList()
            }
            Screen.GUIDE -> {
                if (guideBackScreen == Screen.LIST) renderList() else renderHome()
            }
            Screen.GUIDE_DETAIL -> {
                val cur = currentExerciseId
                if (guideOpenedFromFocus && cur != null) enterFocus(cur) else renderGuide()
            }
        }
    }

    // ==================== STORAGE (OFFLINE) ====================
    private fun loadData() {
        routines.clear()
        val prefs = getSharedPreferences("gymwatch_prefs", Context.MODE_PRIVATE)
        val raw = prefs.getString("data_json", null)
        if (raw != null) {
            try {
                val root = JSONObject(raw)
                activeRoutineId = root.optString("activeId", "w1")
                val arr = root.getJSONArray("workouts")
                for (i in 0 until arr.length()) {
                    val wObj = arr.getJSONObject(i)
                    val exList = mutableListOf<ExerciseItem>()
                    val exArr = wObj.getJSONArray("exercises")
                    for (j in 0 until exArr.length()) {
                        val e = exArr.getJSONObject(j)
                        exList.add(
                            ExerciseItem(
                                e.getString("id"),
                                e.optString("icon", "chest"),
                                e.getString("name"),
                                e.optDouble("weight", 30.0),
                                e.optInt("sets", 4),
                                e.optInt("reps", 10),
                                e.optInt("rest", 60),
                                e.optInt("completedSets", 0)
                            )
                        )
                    }
                    routines.add(WorkoutRoutine(wObj.getString("id"), wObj.getString("name"), exList))
                }
            } catch (_: Exception) {
                routines.clear()
            }
        }

        if (routines.isEmpty()) {
            routines.add(
                WorkoutRoutine(
                    "w1",
                    "Scheda Full Body",
                    mutableListOf(
                        ExerciseItem("e1", "chest", "Chest Press", 32.5, 4, 10, 60, 0),
                        ExerciseItem("e2", "lat", "Lat Machine", 45.0, 4, 10, 90, 0),
                        ExerciseItem("e3", "legpress", "Leg Press", 100.0, 4, 10, 90, 0),
                        ExerciseItem("e4", "adductor", "Adductor", 35.0, 3, 15, 60, 0),
                        ExerciseItem("e5", "abs", "Abdominal Crunch", 25.0, 3, 15, 45, 0)
                    )
                )
            )
            activeRoutineId = "w1"
            saveData()
        }
    }

    private fun saveData() {
        try {
            val root = JSONObject()
            root.put("activeId", activeRoutineId)
            val arr = JSONArray()
            for (w in routines) {
                val wObj = JSONObject()
                wObj.put("id", w.id)
                wObj.put("name", w.name)
                val exArr = JSONArray()
                for (e in w.exercises) {
                    val eObj = JSONObject()
                    eObj.put("id", e.id)
                    eObj.put("icon", e.icon)
                    eObj.put("name", e.name)
                    eObj.put("weight", e.weight)
                    eObj.put("sets", e.sets)
                    eObj.put("reps", e.reps)
                    eObj.put("rest", e.rest)
                    eObj.put("completedSets", e.completedSets)
                    exArr.put(eObj)
                }
                wObj.put("exercises", exArr)
                arr.put(wObj)
            }
            root.put("workouts", arr)
            getSharedPreferences("gymwatch_prefs", Context.MODE_PRIVATE)
                .edit().putString("data_json", root.toString()).apply()
        } catch (_: Exception) {}
    }

    private fun activeRoutine(): WorkoutRoutine {
        return routines.find { it.id == activeRoutineId } ?: routines[0]
    }

    private fun specFor(id: String): MachineSpec {
        return catalog.find { it.id == id } ?: catalog[0]
    }

    private fun fmtWeight(w: Double): String {
        return if (w % 1.0 == 0.0) "${w.toInt()}" else "$w"
    }

    private fun fmtTime(ms: Long): String {
        val sec = max(0, ceil(ms / 1000.0).toInt())
        return "%02d:%02d".format(sec / 60, sec % 60)
    }

    // ==================== HELPERS UI ====================
    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).roundToInt()

    private fun pillBg(fill: Int, radiusDp: Int, strokeColor: Int = Color.TRANSPARENT, strokeDp: Int = 0): GradientDrawable {
        return GradientDrawable().apply {
            setColor(fill)
            cornerRadius = dp(radiusDp).toFloat()
            if (strokeDp > 0) setStroke(dp(strokeDp), strokeColor)
        }
    }

    private fun makeBtn(label: String, bg: Int, fg: Int, textSp: Float = 12f, radiusDp: Int = 10, padV: Int = 8, padH: Int = 10, stroke: Int = Color.TRANSPARENT, onClick: () -> Unit): TextView {
        return TextView(this).apply {
            text = label
            setTextColor(fg)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, textSp)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            background = pillBg(bg, radiusDp, stroke, if (stroke != Color.TRANSPARENT) 1 else 0)
            setPadding(dp(padH), dp(padV), dp(padH), dp(padV))
            setOnClickListener { onClick() }
        }
    }

    private fun showToastMsg(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun scrollContainer(padH: Int = 12, padV: Int = 10): Pair<ScrollView, LinearLayout> {
        val scroll = ScrollView(this).apply {
            setBackgroundColor(bgColor)
            isVerticalScrollBarEnabled = false
        }
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(padH), dp(padV), dp(padH), dp(14))
        }
        scroll.addView(box)
        return scroll to box
    }

    private fun setScreenContent(view: View) {
        rootContainer.removeAllViews()
        rootContainer.addView(view)
    }

    // ==================== 0. HOME SCREEN ====================
    private fun renderHome() {
        currentScreen = Screen.HOME
        val (scroll, box) = scrollContainer(12, 12)
        val routine = activeRoutine()
        val done = routine.exercises.count { it.completedSets >= it.sets }

        // Header
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, dp(8))
        }
        val stripe = View(this).apply {
            background = pillBg(accentColor, 2)
            layoutParams = LinearLayout.LayoutParams(dp(4), dp(16)).apply { rightMargin = dp(6) }
        }
        val brand = TextView(this).apply {
            text = "GYMWATCH"
            setTextColor(textColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            setTypeface(null, Typeface.BOLD_ITALIC)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        val badge = TextView(this).apply {
            text = "PRO"
            setTextColor(accentColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 9f)
            typeface = Typeface.DEFAULT_BOLD
            background = pillBg(surfaceColor, 5, borderColor, 1)
            setPadding(dp(6), dp(2), dp(6), dp(2))
        }
        header.addView(stripe)
        header.addView(brand)
        header.addView(badge)
        box.addView(header)

        // Hero Card
        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = pillBg(surfaceColor, 12, accentColor, 1)
            setPadding(dp(10), dp(9), dp(10), dp(9))
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                topMargin = dp(4)
                bottomMargin = dp(10)
            }
        }
        hero.addView(TextView(this).apply {
            text = "SCHEDA ATTIVA"
            setTextColor(mutedColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 9f)
            typeface = Typeface.DEFAULT_BOLD
        })
        hero.addView(TextView(this).apply {
            text = routine.name
            setTextColor(textColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            typeface = Typeface.DEFAULT_BOLD
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
        })
        hero.addView(TextView(this).apply {
            text = "$done / ${routine.exercises.size} completati"
            setTextColor(accentColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            typeface = Typeface.DEFAULT_BOLD
        })
        box.addView(hero)

        // Main CTA
        box.addView(makeBtn("▶ INIZIA ALLENAMENTO", accentColor, Color.parseColor("#1A1A1A"), 12f, 12, 12, 10) {
            renderList()
        }.apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(8)
            }
        })

        // Secondary Row
        val secRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        secRow.addView(makeBtn("ⓘ GUIDA", surfaceColor, textColor, 11f, 10, 10, 6, borderColor) {
            guideBackScreen = Screen.HOME
            renderGuide()
        }.apply {
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { rightMargin = dp(6) }
        })
        secRow.addView(makeBtn("☰ SCHEDE", surfaceColor, textColor, 11f, 10, 10, 6, borderColor) {
            showWorkoutPickerOverlay()
        }.apply {
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        })
        box.addView(secRow)

        setScreenContent(scroll)
    }

    // ==================== 1. MACHINE LIST ====================
    private fun renderList() {
        currentScreen = Screen.LIST
        val (scroll, box) = scrollContainer(10, 10)
        val routine = activeRoutine()

        // Top nav
        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(6)
            }
        }
        topBar.addView(makeBtn("⌂", surfaceColor, textColor, 13f, 8, 6, 9, borderColor) { renderHome() })
        topBar.addView(makeBtn("${routine.name} ▾", surfaceColor, accentColor, 11f, 8, 7, 8, borderColor) {
            showWorkoutPickerOverlay()
        }.apply {
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                leftMargin = dp(6)
                rightMargin = dp(6)
            }
        })
        topBar.addView(makeBtn("↺", surfaceColor, textColor, 13f, 8, 6, 9, borderColor) {
            routine.exercises.forEach { it.completedSets = 0 }
            saveData()
            renderList()
            showToastMsg("Serie azzerate!")
        })
        box.addView(topBar)

        val doneCount = routine.exercises.count { it.completedSets >= it.sets }
        box.addView(TextView(this).apply {
            text = "$doneCount / ${routine.exercises.size} completati"
            setTextColor(mutedColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(6))
        })

        val sorted = routine.exercises.sortedWith(compareBy<ExerciseItem> { it.completedSets >= it.sets }
            .thenByDescending { it.completedSets in 1 until it.sets })

        if (sorted.isEmpty()) {
            box.addView(TextView(this).apply {
                text = "Scheda vuota.\nAggiungi un macchinario!"
                setTextColor(mutedColor)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                gravity = Gravity.CENTER
                setPadding(0, dp(16), 0, dp(16))
            })
        } else {
            for (ex in sorted) {
                val isDone = ex.completedSets >= ex.sets
                val inProg = ex.completedSets > 0 && !isDone
                val card = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    alpha = if (isDone) 0.52f else 1f
                    background = pillBg(
                        surfaceColor, 12,
                        if (inProg) accentColor else borderColor,
                        if (inProg) 2 else 1
                    )
                    setPadding(dp(8), dp(7), dp(8), dp(7))
                    layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                        bottomMargin = dp(7)
                    }
                    setOnClickListener { enterFocus(ex.id) }
                }

                val iconWrap = FrameLayout(this).apply {
                    background = pillBg(deepColor, 8)
                    setPadding(dp(3), dp(3), dp(3), dp(3))
                    layoutParams = LinearLayout.LayoutParams(dp(30), dp(30)).apply { rightMargin = dp(8) }
                }
                iconWrap.addView(MachineIconView(this, ex.icon))
                card.addView(iconWrap)

                val info = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                }
                info.addView(TextView(this).apply {
                    text = ex.name
                    setTextColor(textColor)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                    typeface = Typeface.DEFAULT_BOLD
                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.END
                })
                val wStr = if (ex.weight > 0) "${fmtWeight(ex.weight)}kg • " else ""
                info.addView(TextView(this).apply {
                    text = "$wStr${ex.reps}r • ${ex.rest}s"
                    setTextColor(mutedColor)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
                })
                card.addView(info)

                card.addView(TextView(this).apply {
                    text = if (isDone) "✓" else "${ex.completedSets}/${ex.sets}"
                    setTextColor(if (inProg) Color.parseColor("#1A1A1A") else if (isDone) accentColor else mutedColor)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, if (isDone) 14f else 11f)
                    typeface = Typeface.DEFAULT_BOLD
                    background = if (inProg) pillBg(accentColor, 6) else if (isDone) null else pillBg(deepColor, 6)
                    setPadding(dp(6), dp(3), dp(6), dp(3))
                })

                box.addView(card)
            }
        }

        val footer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                topMargin = dp(4)
            }
        }
        footer.addView(makeBtn("+ MACCHINARIO", accentColor, Color.parseColor("#1A1A1A"), 11f, 10, 9, 8) {
            openEditor(null, null)
        }.apply {
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { rightMargin = dp(6) }
        })
        footer.addView(makeBtn("ⓘ GUIDA", surfaceColor, textColor, 11f, 10, 9, 8, borderColor) {
            guideBackScreen = Screen.LIST
            renderGuide()
        })
        box.addView(footer)

        setScreenContent(scroll)
    }

    // ==================== 2. FOCUS MODE (SQUARE PERIMETER RING) ====================
    private fun enterFocus(exId: String) {
        stopTimer()
        currentExerciseId = exId
        val ex = activeRoutine().exercises.find { it.id == exId } ?: return
        totalRestMs = ex.rest * 1000L
        remainingMs = totalRestMs
        timerState = "idle"
        renderFocus()
    }

    private fun renderFocus() {
        currentScreen = Screen.FOCUS
