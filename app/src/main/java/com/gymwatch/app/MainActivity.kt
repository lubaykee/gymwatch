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
import kotlin.math.*

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
        val id: String, val cat: String, val name: String, val shortName: String,
        val defSets: Int, val defReps: Int, val defWeight: Double, val defRest: Int,
        val target: String, val tips: String
    )

    data class ExerciseItem(
        val id: String, var icon: String, var name: String, var weight: Double,
        var sets: Int, var reps: Int, var rest: Int, var completedSets: Int = 0
    )

    data class WorkoutRoutine(
        val id: String, var name: String, val exercises: MutableList<ExerciseItem>
    )

    private val categories = listOf(
        "Schiena e spalle", "Petto e braccia", "Gambe e glutei", "Addome e core", "Cardio & Corpo libero"
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

    private var countDownTimer: CountDownTimer? = null
    private var timerState = "idle"
    private var remainingMs = 60000L
    private var totalRestMs = 60000L

    private lateinit var rootContainer: FrameLayout
    private lateinit var focusRingFrame: SquareRingFrame

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
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
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
            Screen.FOCUS -> { stopTimer(); currentExerciseId = null; renderList() }
            Screen.EDITOR -> {
                val cur = currentExerciseId
                if (cur != null && editingExerciseId == cur) enterFocus(cur) else renderList()
            }
            Screen.GUIDE -> if (guideBackScreen == Screen.LIST) renderList() else renderHome()
            Screen.GUIDE_DETAIL -> {
                val cur = currentExerciseId
                if (guideOpenedFromFocus && cur != null) enterFocus(cur) else renderGuide()
            }
        }
    }

    private fun drawVectorArrow(c: Canvas, x1: Float, y1: Float, x2: Float, y2: Float, p: Paint, headLen: Double) {
        c.drawLine(x1, y1, x2, y2, p)
        val ang = atan2((y2 - y1).toDouble(), (x2 - x1).toDouble())
        c.drawLine(x2, y2, (x2 - headLen * cos(ang - 0.55)).toFloat(), (y2 - headLen * sin(ang - 0.55)).toFloat(), p)
        c.drawLine(x2, y2, (x2 - headLen * cos(ang + 0.55)).toFloat(), (y2 - headLen * sin(ang + 0.55)).toFloat(), p)
    }

    // ==================== STORAGE ====================
    private fun loadData() {
        routines.clear()
        val raw = getSharedPreferences("gymwatch_prefs", Context.MODE_PRIVATE).getString("data_json", null)
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
                        exList.add(ExerciseItem(
                            e.getString("id"), e.optString("icon", "chest"), e.getString("name"),
                            e.optDouble("weight", 30.0), e.optInt("sets", 4), e.optInt("reps", 10),
                            e.optInt("rest", 60), e.optInt("completedSets", 0)
                        ))
                    }
                    routines.add(WorkoutRoutine(wObj.getString("id"), wObj.getString("name"), exList))
                }
            } catch (_: Exception) { routines.clear() }
        }
        if (routines.isEmpty()) {
            routines.add(WorkoutRoutine("w1", "Scheda Full Body", mutableListOf(
                ExerciseItem("e1", "chest", "Chest Press", 32.5, 4, 10, 60, 0),
                ExerciseItem("e2", "lat", "Lat Machine", 45.0, 4, 10, 90, 0),
                ExerciseItem("e3", "legpress", "Leg Press", 100.0, 4, 10, 90, 0),
                ExerciseItem("e4", "adductor", "Adductor", 35.0, 3, 15, 60, 0),
                ExerciseItem("e5", "abs", "Abdominal Crunch", 25.0, 3, 15, 45, 0)
            )))
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
                wObj.put("id", w.id); wObj.put("name", w.name)
                val exArr = JSONArray()
                for (e in w.exercises) {
                    val eObj = JSONObject()
                    eObj.put("id", e.id); eObj.put("icon", e.icon); eObj.put("name", e.name)
                    eObj.put("weight", e.weight); eObj.put("sets", e.sets); eObj.put("reps", e.reps)
                    eObj.put("rest", e.rest); eObj.put("completedSets", e.completedSets)
                    exArr.put(eObj)
                }
                wObj.put("exercises", exArr); arr.put(wObj)
            }
            root.put("workouts", arr)
            getSharedPreferences("gymwatch_prefs", Context.MODE_PRIVATE).edit().putString("data_json", root.toString()).apply()
        } catch (_: Exception) {}
    }

    private fun activeRoutine(): WorkoutRoutine = routines.find { it.id == activeRoutineId } ?: routines[0]
    private fun specFor(id: String): MachineSpec = catalog.find { it.id == id } ?: catalog[0]
    private fun fmtWeight(w: Double): String = if (w % 1.0 == 0.0) "${w.toInt()}" else "$w"
    private fun fmtTime(ms: Long): String {
        val sec = max(0, ceil(ms / 1000.0).toInt())
        return "%02d:%02d".format(sec / 60, sec % 60)
    }

    // ==================== HELPERS UI ====================
    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).roundToInt()

    private fun pillBg(fill: Int, radiusDp: Int, strokeColor: Int = Color.TRANSPARENT, strokeDp: Int = 0): GradientDrawable {
        return GradientDrawable().apply {
            setColor(fill); cornerRadius = dp(radiusDp).toFloat()
            if (strokeDp > 0) setStroke(dp(strokeDp), strokeColor)
        }
    }

    private fun makeBtn(label: String, bg: Int, fg: Int, textSp: Float = 12f, radiusDp: Int = 10, padV: Int = 8, padH: Int = 10, stroke: Int = Color.TRANSPARENT, onClick: () -> Unit): TextView {
        return TextView(this).apply {
            text = label; setTextColor(fg); setTextSize(TypedValue.COMPLEX_UNIT_SP, textSp)
            typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER
            background = pillBg(bg, radiusDp, stroke, if (stroke != Color.TRANSPARENT) 1 else 0)
            setPadding(dp(padH), dp(padV), dp(padH), dp(padV))
            setOnClickListener { onClick() }
        }
    }

    private fun showToastMsg(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    private fun scrollContainer(padH: Int = 12, padV: Int = 10): Pair<ScrollView, LinearLayout> {
        val scroll = ScrollView(this).apply { setBackgroundColor(bgColor); isVerticalScrollBarEnabled = false }
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(padH), dp(padV), dp(padH), dp(14)) }
        scroll.addView(box)
        return scroll to box
    }

    private fun setScreenContent(view: View) {
        rootContainer.removeAllViews()
        rootContainer.addView(view)
    }

    // ==================== 0. HOME ====================
    private fun renderHome() {
        currentScreen = Screen.HOME
        val (scroll, box) = scrollContainer(12, 12)
        val routine = activeRoutine()
        val done = routine.exercises.count { it.completedSets >= it.sets }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(0, 0, 0, dp(8))
        }
        header.addView(View(this).apply {
            background = pillBg(accentColor, 2)
            layoutParams = LinearLayout.LayoutParams(dp(4), dp(16)).apply { rightMargin = dp(6) }
        })
        header.addView(TextView(this).apply {
            text = "GYMWATCH"; setTextColor(textColor); setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            setTypeface(null, Typeface.BOLD_ITALIC)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        })
        header.addView(TextView(this).apply {
            text = "PRO"; setTextColor(accentColor); setTextSize(TypedValue.COMPLEX_UNIT_SP, 9f)
            typeface = Typeface.DEFAULT_BOLD; background = pillBg(surfaceColor, 5, borderColor, 1)
            setPadding(dp(6), dp(2), dp(6), dp(2))
        })
        box.addView(header)

        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; background = pillBg(surfaceColor, 12, accentColor, 1)
            setPadding(dp(10), dp(9), dp(10), dp(9))
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                topMargin = dp(4); bottomMargin = dp(10)
            }
        }
        hero.addView(TextView(this).apply { text = "SCHEDA ATTIVA"; setTextColor(mutedColor); setTextSize(TypedValue.COMPLEX_UNIT_SP, 9f); typeface = Typeface.DEFAULT_BOLD })
        hero.addView(TextView(this).apply {
            text = routine.name; setTextColor(textColor); setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            typeface = Typeface.DEFAULT_BOLD; maxLines = 1; ellipsize = TextUtils.TruncateAt.END
        })
        hero.addView(TextView(this).apply { text = "$done / ${routine.exercises.size} completati"; setTextColor(accentColor); setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f); typeface = Typeface.DEFAULT_BOLD })
        box.addView(hero)

        box.addView(makeBtn("▶ INIZIA ALLENAMENTO", accentColor, Color.parseColor("#1A1A1A"), 12f, 12, 12, 10) { renderList() }.apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dp(8) }
        })

        val secRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        secRow.addView(makeBtn("ⓘ GUIDA", surfaceColor, textColor, 11f, 10, 10, 6, borderColor) {
            guideBackScreen = Screen.HOME; renderGuide()
        }.apply { layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { rightMargin = dp(6) } })
        secRow.addView(makeBtn("☰ SCHEDE", surfaceColor, textColor, 11f, 10, 10, 6, borderColor) {
            showWorkoutPickerOverlay()
        }.apply { layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f) })
        box.addView(secRow)
        setScreenContent(scroll)
    }

    // ==================== 1. LIST ====================
    private fun renderList() {
        currentScreen = Screen.LIST
        val (scroll, box) = scrollContainer(10, 10)
        val routine = activeRoutine()

        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dp(6) }
        }
        topBar.addView(makeBtn("⌂", surfaceColor, textColor, 13f, 8, 6, 9, borderColor) { renderHome() })
        topBar.addView(makeBtn("${routine.name} ▾", surfaceColor, accentColor, 11f, 8, 7, 8, borderColor) { showWorkoutPickerOverlay() }.apply {
            maxLines = 1; ellipsize = TextUtils.TruncateAt.END
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { leftMargin = dp(6); rightMargin = dp(6) }
        })
        topBar.addView(makeBtn("↺", surfaceColor, textColor, 13f, 8, 6, 9, borderColor) {
            routine.exercises.forEach { it.completedSets = 0 }; saveData(); renderList(); showToastMsg("Serie azzerate!")
        })
        box.addView(topBar)

        val doneCount = routine.exercises.count { it.completedSets >= it.sets }
        box.addView(TextView(this).apply {
            text = "$doneCount / ${routine.exercises.size} completati"; setTextColor(mutedColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f); typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER; setPadding(0, 0, 0, dp(6))
        })

        val sorted = routine.exercises.sortedWith(compareBy<ExerciseItem> { it.completedSets >= it.sets }.thenByDescending { it.completedSets in 1 until it.sets })
        if (sorted.isEmpty()) {
            box.addView(TextView(this).apply {
                text = "Scheda vuota.\nAggiungi un macchinario!"; setTextColor(mutedColor)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f); gravity = Gravity.CENTER; setPadding(0, dp(16), 0, dp(16))
            })
        } else {
            for (ex in sorted) {
                val isDone = ex.completedSets >= ex.sets
                val inProg = ex.completedSets > 0 && !isDone
                val card = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
                    alpha = if (isDone) 0.52f else 1f
                    background = pillBg(surfaceColor, 12, if (inProg) accentColor else borderColor, if (inProg) 2 else 1)
                    setPadding(dp(8), dp(7), dp(8), dp(7))
                    layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dp(7) }
                    setOnClickListener { enterFocus(ex.id) }
                }
                val iconWrap = FrameLayout(this).apply {
                    background = pillBg(deepColor, 8); setPadding(dp(3), dp(3), dp(3), dp(3))
                    layoutParams = LinearLayout.LayoutParams(dp(30), dp(30)).apply { rightMargin = dp(8) }
                }
                iconWrap.addView(MachineIconView(this, ex.icon))
                card.addView(iconWrap)

                val info = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f) }
                info.addView(TextView(this).apply { text = ex.name; setTextColor(textColor); setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f); typeface = Typeface.DEFAULT_BOLD; maxLines = 1; ellipsize = TextUtils.TruncateAt.END })
                val wStr = if (ex.weight > 0) "${fmtWeight(ex.weight)}kg • " else ""
                info.addView(TextView(this).apply { text = "$wStr${ex.reps}r • ${ex.rest}s"; setTextColor(mutedColor); setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f) })
                card.addView(info)

                card.addView(TextView(this).apply {
                    text = if (isDone) "✓" else "${ex.completedSets}/${ex.sets}"
                    setTextColor(if (inProg) Color.parseColor("#1A1A1A") else if (isDone) accentColor else mutedColor)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, if (isDone) 14f else 11f); typeface = Typeface.DEFAULT_BOLD
                    background = if (inProg) pillBg(accentColor, 6) else if (isDone) null else pillBg(deepColor, 6)
                    setPadding(dp(6), dp(3), dp(6), dp(3))
                })
                box.addView(card)
            }
        }

        val footer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(4) }
        }
        footer.addView(makeBtn("+ MACCHINARIO", accentColor, Color.parseColor("#1A1A1A"), 11f, 10, 9, 8) { openEditor(null, null) }.apply {
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { rightMargin = dp(6) }
        })
        footer.addView(makeBtn("ⓘ GUIDA", surfaceColor, textColor, 11f, 10, 9, 8, borderColor) { guideBackScreen = Screen.LIST; renderGuide() })
        box.addView(footer)
        setScreenContent(scroll)
    }

    // ==================== 2. FOCUS ====================
    private fun enterFocus(exId: String) {
        stopTimer()
        currentExerciseId = exId
        val ex = activeRoutine().exercises.find { it.id == exId } ?: return
        totalRestMs = ex.rest * 1000L; remainingMs = totalRestMs; timerState = "idle"
        renderFocus()
    }

    private fun renderFocus() {
        currentScreen = Screen.FOCUS
        val ex = activeRoutine().exercises.find { it.id == currentExerciseId } ?: return

        focusRingFrame = SquareRingFrame(this).apply {
            setBackgroundColor(bgColor)
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }

        val inner = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(12), dp(10), dp(12), dp(10))
            layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }

        val topRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        topRow.addView(makeBtn("⇄ CAMBIA", surfaceColor, textColor, 10f, 8, 4, 8, borderColor) {
            stopTimer(); currentExerciseId = null; renderList()
        })
        topRow.addView(View(this).apply { layoutParams = LinearLayout.LayoutParams(0, 1, 1f) })
        topRow.addView(makeBtn("ⓘ", surfaceColor, textColor, 11f, 8, 4, 8, borderColor) {
            stopTimer(); guideOpenedFromFocus = true; activeGuideSpec = specFor(ex.icon); renderGuideDetail()
        }.apply { layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { rightMargin = dp(4) } })
        topRow.addView(makeBtn("✎", surfaceColor, textColor, 11f, 8, 4, 8, borderColor) {
            stopTimer(); openEditor(ex.id, null)
        })
        inner.addView(topRow)

        val titleRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER; setPadding(0, dp(4), 0, dp(2)) }
        titleRow.addView(MachineIconView(this, ex.icon).apply { layoutParams = LinearLayout.LayoutParams(dp(22), dp(22)).apply { rightMargin = dp(6) } })
        titleRow.addView(TextView(this).apply {
            text = ex.name.uppercase(); setTextColor(textColor); setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            typeface = Typeface.DEFAULT_BOLD; maxLines = 1; ellipsize = TextUtils.TruncateAt.END
        })
        inner.addView(titleRow)

        val pillsRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER; setPadding(0, dp(2), 0, dp(4)) }
        pillsRow.addView(makeBtn("${fmtWeight(ex.weight)} kg ✎", surfaceColor, accentColor, 11f, 8, 4, 8, borderColor) { showQuickWeightOverlay(ex) }.apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { rightMargin = dp(6) }
        })
        pillsRow.addView(makeBtn("${ex.reps} rep ✎", surfaceColor, textColor, 11f, 8, 4, 8, borderColor) { showQuickRepsOverlay(ex) })
        inner.addView(pillsRow)

        val setsRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER; setPadding(0, dp(2), 0, dp(2)) }
        for (i in 1..ex.sets) {
            val isDone = i <= ex.completedSets
            val isNext = i == ex.completedSets + 1
            setsRow.addView(TextView(this).apply {
                text = if (isDone) "✓" else "$i"
                setTextColor(if (isDone) Color.parseColor("#1A1A1A") else if (isNext) textColor else mutedColor)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f); typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER
                background = pillBg(if (isDone) accentColor else surfaceColor, 7, if (isDone || isNext) accentColor else Color.parseColor("#444444"), if (isDone || isNext) 2 else 1)
                layoutParams = LinearLayout.LayoutParams(dp(26), dp(26)).apply { leftMargin = dp(2); rightMargin = dp(2) }
                setOnClickListener { ex.completedSets = if (i <= ex.completedSets) i - 1 else i; saveData(); renderFocus() }
            })
        }
        inner.addView(setsRow)

        inner.addView(TextView(this).apply {
            text = if (timerState == "finished") "00:00" else fmtTime(remainingMs)
            setTextColor(if (timerState == "finished") accentColor else textColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 38f); typeface = Typeface.DEFAULT_BOLD; fontFeatureSettings = "tnum"; gravity = Gravity.CENTER
        })

        val nextSetNum = ex.completedSets + 1
        val isLastPending = ex.completedSets == ex.sets - 1
        val statusText = when (timerState) {
            "running" -> "RECUPERO IN CORSO"
            "paused" -> "IN PAUSA"
            "finished" -> if (isLastPending) "TEMPO SCADUTO • ULTIMA (${ex.sets}ª)!" else "VAI! FAI LA ${nextSetNum}ª SERIE"
            else -> when {
                ex.completedSets >= ex.sets -> "MACCHINARIO COMPLETATO ✓"
                isLastPending && ex.sets > 1 -> "ULTIMA SERIE (${ex.sets}/${ex.sets})"
                else -> "FAI LA ${nextSetNum}ª SERIE"
            }
        }
        inner.addView(TextView(this).apply {
            text = statusText; setTextColor(if (timerState == "running" || timerState == "finished") accentColor else mutedColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 9f); typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER; setPadding(0, 0, 0, dp(4))
        })

        val prog = when (timerState) {
            "finished" -> 0f
            "idle" -> 1f
            else -> if (totalRestMs > 0) remainingMs.toFloat() / totalRestMs else 1f
        }
        focusRingFrame.setProgress(prog)
        inner.addView(View(this).apply { layoutParams = LinearLayout.LayoutParams(1, 0, 1f) })

        val mainBtnText: String
        var mainBg = accentColor
        var mainFg = Color.parseColor("#1A1A1A")
        when (timerState) {
            "running" -> { mainBtnText = "PAUSA"; mainBg = surface2Color; mainFg = textColor }
            "paused" -> mainBtnText = "RIPRENDI"
            else -> mainBtnText = when {
                ex.completedSets >= ex.sets -> "✓ PROSSIMO MACCHINARIO"
                isLastPending && ex.sets > 1 -> "✓ COMPLETA ${ex.sets}ª & FINE"
                ex.sets == 1 -> "✓ COMPLETA MACCHINARIO"
                else -> "FINE ${nextSetNum}ª • RECUPERO (${ex.rest}s)"
            }
        }
        inner.addView(makeBtn(mainBtnText, mainBg, mainFg, 11f, 10, 10, 8) { handleMainFocusAction(ex) }.apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dp(4) }
        })

        val subRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
        if (timerState == "running" || timerState == "paused") {
            subRow.addView(makeBtn("SALTA", Color.TRANSPARENT, mutedColor, 10f, 7, 4, 8, borderColor) {
                stopTimer(); timerState = "idle"; remainingMs = ex.rest * 1000L; renderFocus()
            }.apply { layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { rightMargin = dp(6) } })
        } else if (ex.completedSets >= ex.sets - 1 || ex.sets == 1) {
            subRow.addView(makeBtn("SOLO TIMER", Color.TRANSPARENT, mutedColor, 10f, 7, 4, 8, borderColor) {
                totalRestMs = ex.rest * 1000L; remainingMs = totalRestMs; startCountdown()
            }.apply { layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { rightMargin = dp(6) } })
        }
        subRow.addView(makeBtn("↩ ANNULLA CHECK", Color.TRANSPARENT, mutedColor, 10f, 7, 4, 8, borderColor) {
            if (timerState == "running" || timerState == "paused") { stopTimer(); timerState = "idle"; remainingMs = ex.rest * 1000L }
            if (ex.completedSets > 0) { ex.completedSets -= 1; saveData() }
            renderFocus()
        })
        inner.addView(subRow)

        focusRingFrame.addView(inner)
        setScreenContent(focusRingFrame)
    }

    private fun handleMainFocusAction(ex: ExerciseItem) {
        when (timerState) {
            "running" -> { countDownTimer?.cancel(); countDownTimer = null; timerState = "paused"; setKeepScreenOn(false); renderFocus() }
            "paused" -> startCountdown()
            else -> {
                if (ex.completedSets == ex.sets - 1 && ex.sets > 1) {
                    ex.completedSets = ex.sets; saveData(); stopTimer()
                    showToastMsg("${ex.name} ✓ Completato!"); currentExerciseId = null; renderList(); return
                }
                if (ex.completedSets >= ex.sets) { currentExerciseId = null; renderList(); return }
                ex.completedSets += 1; saveData()
                totalRestMs = ex.rest * 1000L; remainingMs = totalRestMs; startCountdown()
            }
        }
    }

    private fun startCountdown() {
        countDownTimer?.cancel(); timerState = "running"; setKeepScreenOn(true); renderFocus()
        countDownTimer = object : CountDownTimer(remainingMs, 200) {
            override fun onTick(m: Long) { remainingMs = m; if (currentScreen == Screen.FOCUS) renderFocus() }
            override fun onFinish() {
                remainingMs = 0L; timerState = "finished"; setKeepScreenOn(false)
                vibrateAlert(); playBeep(); if (currentScreen == Screen.FOCUS) renderFocus()
            }
        }.start()
    }

    private fun stopTimer() { countDownTimer?.cancel(); countDownTimer = null; setKeepScreenOn(false) }

    // ==================== 3. EDITOR ====================
    private fun openEditor(exId: String?, prefill: MachineSpec?) {
        editingExerciseId = exId
        val existing = if (exId != null) activeRoutine().exercises.find { it.id == exId } else null
        when {
            existing != null -> { edIcon = existing.icon; edName = existing.name; edWeight = existing.weight; edSets = existing.sets; edReps = existing.reps; edRest = existing.rest }
            prefill != null -> { edIcon = prefill.id; edName = prefill.name; edWeight = prefill.defWeight; edSets = prefill.defSets; edReps = prefill.defReps; edRest = prefill.defRest }
            else -> { edIcon = "chest"; edName = ""; edWeight = 30.0; edSets = 4; edReps = 10; edRest = 60 }
        }
        renderEditor()
    }

    private fun renderEditor() {
        currentScreen = Screen.EDITOR
        val (scroll, box) = scrollContainer(10, 10)

        val topBar = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(0, 0, 0, dp(8)) }
        topBar.addView(makeBtn("←", surfaceColor, textColor, 13f, 8, 5, 9, borderColor) {
            val cur = currentExerciseId; if (cur != null && editingExerciseId == cur) enterFocus(cur) else renderList()
        })
        topBar.addView(TextView(this).apply {
            text = if (editingExerciseId != null) "MODIFICA" else "NUOVO ESERCIZIO"; setTextColor(accentColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f); typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        })
        topBar.addView(View(this).apply { layoutParams = LinearLayout.LayoutParams(dp(28), 1) })
        box.addView(topBar)

        val nameInput = EditText(this).apply {
            setText(edName); hint = "Nome macchinario"; setTextColor(textColor); setHintTextColor(mutedColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f); typeface = Typeface.DEFAULT_BOLD
            background = pillBg(surfaceColor, 10, borderColor, 1); setPadding(dp(10), dp(8), dp(10), dp(8))
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dp(10) }
        }

        for (cat in categories) {
            val divRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(0, dp(4), 0, dp(6)) }
            divRow.addView(TextView(this).apply { text = cat.uppercase(); setTextColor(accentColor); setTextSize(TypedValue.COMPLEX_UNIT_SP, 9f); typeface = Typeface.DEFAULT_BOLD })
            divRow.addView(View(this).apply { setBackgroundColor(borderColor); layoutParams = LinearLayout.LayoutParams(0, dp(1), 1f).apply { leftMargin = dp(6) } })
            box.addView(divRow)

            val items = catalog.filter { it.cat == cat }
            var rowLayout: LinearLayout? = null
            items.forEachIndexed { idx, spec ->
                if (idx % 3 == 0) {
                    rowLayout = LinearLayout(this).apply {
                        orientation = LinearLayout.HORIZONTAL
                        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dp(6) }
                    }
                    box.addView(rowLayout)
                }
                val selected = edIcon == spec.id
                val cell = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER
                    background = pillBg(if (selected) Color.parseColor("#332E14") else surfaceColor, 10, if (selected) accentColor else borderColor, if (selected) 2 else 1)
                    setPadding(dp(4), dp(6), dp(4), dp(5))
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { if (idx % 3 < 2) rightMargin = dp(5) }
                    setOnClickListener {
                        edIcon = spec.id
                        val curText = nameInput.text.toString().trim()
                        val isCat = catalog.any { it.name.equals(curText, ignoreCase = true) }
                        edName = if (curText.isEmpty() || isCat) spec.name else curText
                        renderEditor()
                    }
                }
                cell.addView(MachineIconView(this, spec.id).apply { layoutParams = LinearLayout.LayoutParams(dp(24), dp(24)) })
                cell.addView(TextView(this).apply {
                    text = spec.shortName; setTextColor(if (selected) accentColor else textColor)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 9f); typeface = Typeface.DEFAULT_BOLD
                    maxLines = 1; ellipsize = TextUtils.TruncateAt.END; setPadding(0, dp(3), 0, 0)
                })
                rowLayout?.addView(cell)
            }
            val rem = items.size % 3
            if (rem != 0) {
                for (k in 0 until (3 - rem)) rowLayout?.addView(View(this).apply { layoutParams = LinearLayout.LayoutParams(0, 1, 1f).apply { if (k < 3 - rem - 1) rightMargin = dp(5) } })
            }
        }

        box.addView(TextView(this).apply { text = "NOME ESERCIZIO"; setTextColor(mutedColor); setTextSize(TypedValue.COMPLEX_UNIT_SP, 9f); typeface = Typeface.DEFAULT_BOLD; setPadding(0, dp(4), 0, dp(3)) })
        box.addView(nameInput)

        val wLabel = TextView(this).apply { text = "${fmtWeight(edWeight)} kg"; setTextColor(textColor); setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f); typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER; setPadding(0, dp(2), 0, dp(6)) }
        val wCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; background = pillBg(surfaceColor, 10, borderColor, 1); setPadding(dp(8), dp(6), dp(8), dp(8))
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dp(8) }
        }
        wCard.addView(wLabel)
        val wRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        listOf(-5.0, -2.5, 2.5, 5.0).forEachIndexed { idx, d ->
            val lbl = if (d > 0) "+${fmtWeight(d)}" else fmtWeight(d)
            wRow.addView(makeBtn(lbl, deepColor, if (d > 0) accentColor else textColor, 11f, 7, 6, 4, borderColor) {
                edWeight = max(0.0, ((edWeight + d) * 10).roundToInt() / 10.0); wLabel.text = "${fmtWeight(edWeight)} kg"
            }.apply { layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { if (idx < 3) rightMargin = dp(4) } })
        }
        wCard.addView(wRow); box.addView(wCard)

        val srRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dp(8) } }
        srRow.addView(makeStepper("SERIE", { "$edSets" }) { d -> edSets = min(10, max(1, edSets + d)) }.apply { layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { rightMargin = dp(6) } })
        srRow.addView(makeStepper("RIPETIZIONI", { "$edReps" }) { d -> edReps = min(99, max(1, edReps + d)) }.apply { layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f) })
        box.addView(srRow)

        box.addView(makeStepper("RECUPERO (SEC)", { "${edRest}s" }) { d -> edRest = min(300, max(15, edRest + d * 15)) }.apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dp(10) }
        })

        val actRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        actRow.addView(makeBtn("SALVA", accentColor, Color.parseColor("#1A1A1A"), 12f, 10, 10, 10) {
            val finalName = nameInput.text.toString().trim().ifEmpty { specFor(edIcon).name }
            val routine = activeRoutine()
            if (editingExerciseId != null) {
                val ex = routine.exercises.find { it.id == editingExerciseId }
                if (ex != null) {
                    ex.icon = edIcon; ex.name = finalName; ex.weight = edWeight; ex.sets = edSets; ex.reps = edReps; ex.rest = edRest
                    if (ex.completedSets > ex.sets) ex.completedSets = ex.sets
                }
            } else {
                routine.exercises.add(ExerciseItem("e${System.currentTimeMillis()}", edIcon, finalName, edWeight, edSets, edReps, edRest, 0))
            }
            saveData(); showToastMsg("Salvato!")
            val cur = currentExerciseId
            if (cur != null && editingExerciseId == cur) enterFocus(cur) else renderList()
        }.apply { layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f) })

        if (editingExerciseId != null) {
            actRow.addView(makeBtn("ELIMINA", surfaceColor, dangerColor, 11f, 10, 10, 10, dangerColor) {
                activeRoutine().exercises.removeAll { it.id == editingExerciseId }
                if (currentExerciseId == editingExerciseId) currentExerciseId = null
                saveData(); renderList(); showToastMsg("Rimosso")
            }.apply { layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { leftMargin = dp(6) } })
        }
        box.addView(actRow)
        setScreenContent(scroll)
    }

    private fun makeStepper(title: String, valProvider: () -> String, onChange: (Int) -> Unit): LinearLayout {
        val wrap = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; background = pillBg(surfaceColor, 10, borderColor, 1); setPadding(dp(6), dp(5), dp(6), dp(6)) }
        wrap.addView(TextView(this).apply { text = title; setTextColor(mutedColor); setTextSize(TypedValue.COMPLEX_UNIT_SP, 9f); typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER })
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(0, dp(3), 0, 0) }
        val valTv = TextView(this).apply { text = valProvider(); setTextColor(textColor); setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f); typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER; layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f) }
        row.addView(makeBtn("-", deepColor, accentColor, 14f, 7, 3, 9) { onChange(-1); valTv.text = valProvider() })
        row.addView(valTv)
        row.addView(makeBtn("+", deepColor, accentColor, 14f, 7, 3, 9) { onChange(1); valTv.text = valProvider() })
        wrap.addView(row)
        return wrap
    }

    // ==================== 4. GUIDA ====================
    private fun renderGuide() {
        currentScreen = Screen.GUIDE
        val (scroll, box) = scrollContainer(10, 10)
        val topBar = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(0, 0, 0, dp(8)) }
        topBar.addView(makeBtn("←", surfaceColor, textColor, 13f, 8, 5, 9, borderColor) { if (guideBackScreen == Screen.LIST) renderList() else renderHome() })
        topBar.addView(TextView(this).apply { text = "GUIDA MACCHINARI"; setTextColor(accentColor); setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f); typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER; layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f) })
        topBar.addView(View(this).apply { layoutParams = LinearLayout.LayoutParams(dp(28), 1) })
        box.addView(topBar)

        val filterList = listOf("Tutti" to "Tutti", "Schiena e spalle" to "Schiena", "Petto e braccia" to "Petto/Br.", "Gambe e glutei" to "Gambe", "Addome e core" to "Core", "Cardio & Corpo libero" to "Cardio/Lib.")
        for (rowIdx in 0..1) {
            val fRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dp(5) } }
            for (colIdx in 0..2) {
                val (catId, lbl) = filterList[rowIdx * 3 + colIdx]
                val active = guideFilterCat == catId
                fRow.addView(makeBtn(lbl, if (active) accentColor else surfaceColor, if (active) Color.parseColor("#1A1A1A") else textColor, 10f, 8, 5, 4, if (active) accentColor else borderColor) {
                    guideFilterCat = catId; renderGuide()
                }.apply { layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { if (colIdx < 2) rightMargin = dp(4) } })
            }
            box.addView(fRow)
        }

        val items = if (guideFilterCat == "Tutti") catalog else catalog.filter { it.cat == guideFilterCat }
        for (spec in items) {
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
                background = pillBg(surfaceColor, 12, borderColor, 1); setPadding(dp(8), dp(7), dp(8), dp(7))
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(4) }
                setOnClickListener { guideOpenedFromFocus = false; activeGuideSpec = spec; renderGuideDetail() }
            }
            val iconWrap = FrameLayout(this).apply { background = pillBg(deepColor, 8); setPadding(dp(3), dp(3), dp(3), dp(3)); layoutParams = LinearLayout.LayoutParams(dp(30), dp(30)).apply { rightMargin = dp(8) } }
            iconWrap.addView(MachineIconView(this, spec.id)); card.addView(iconWrap)
            val info = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f) }
            info.addView(TextView(this).apply { text = spec.name; setTextColor(textColor); setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f); typeface = Typeface.DEFAULT_BOLD })
            info.addView(TextView(this).apply { text = spec.cat; setTextColor(mutedColor); setTextSize(TypedValue.COMPLEX_UNIT_SP, 9f) })
            card.addView(info)
            card.addView(TextView(this).apply { text = "›"; setTextColor(accentColor); setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f); typeface = Typeface.DEFAULT_BOLD })
            box.addView(card)
        }
        setScreenContent(scroll)
    }

    private fun renderGuideDetail() {
        currentScreen = Screen.GUIDE_DETAIL
        val (scroll, box) = scrollContainer(10, 10)
        val spec = activeGuideSpec

        val topBar = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(0, 0, 0, dp(6)) }
        topBar.addView(makeBtn("←", surfaceColor, textColor, 13f, 8, 5, 9, borderColor) {
            val cur = currentExerciseId; if (guideOpenedFromFocus && cur != null) enterFocus(cur) else renderGuide()
        })
        topBar.addView(TextView(this).apply { text = spec.cat.uppercase(); setTextColor(accentColor); setTextSize(TypedValue.COMPLEX_UNIT_SP, 9f); typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER; layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f) })
        topBar.addView(View(this).apply { layoutParams = LinearLayout.LayoutParams(dp(28), 1) })
        box.addView(topBar)

        val head = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(0, 0, 0, dp(8)) }
        head.addView(MachineIconView(this, spec.id).apply { layoutParams = LinearLayout.LayoutParams(dp(28), dp(28)).apply { rightMargin = dp(8) } })
        head.addView(TextView(this).apply { text = spec.name.uppercase(); setTextColor(textColor); setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f); typeface = Typeface.DEFAULT_BOLD })
        box.addView(head)

        val diagBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; background = pillBg(deepColor, 12, borderColor, 1); setPadding(dp(6), dp(6), dp(6), dp(6))
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dp(8) }
        }
        diagBox.addView(PostureDiagramView(this, spec.id).apply { layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(86)) })
        diagBox.addView(TextView(this).apply { text = "● Bianco: Postura    ● Giallo: Presa & Movimento"; setTextColor(mutedColor); setTextSize(TypedValue.COMPLEX_UNIT_SP, 8f); gravity = Gravity.CENTER })
        box.addView(diagBox)

        box.addView(makeInfoCard("A COSA SERVE", spec.target))
        box.addView(makeInfoCard("POSIZIONE, PRESA & MOVIMENTO", spec.tips))
        box.addView(makeBtn("+ AGGIUNGI ALLA SCHEDA", accentColor, Color.parseColor("#1A1A1A"), 11f, 10, 10, 8) {
            currentExerciseId = null; openEditor(null, spec)
        }.apply { layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(4) } })
        setScreenContent(scroll)
    }

    private fun makeInfoCard(title: String, body: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; background = pillBg(surfaceColor, 10, borderColor, 1); setPadding(dp(10), dp(8), dp(10), dp(8))
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dp(7) }
            addView(TextView(context).apply { text = title; setTextColor(accentColor); setTextSize(TypedValue.COMPLEX_UNIT_SP, 9f); typeface = Typeface.DEFAULT_BOLD; setPadding(0, 0, 0, dp(3)) })
            addView(TextView(context).apply { text = body; setTextColor(Color.parseColor("#EAEAEA")); setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f) })
        }
    }

    // ==================== OVERLAYS ====================
    private fun showQuickWeightOverlay(ex: ExerciseItem) {
        val overlay = makeOverlayBox("MODIFICA CARICO")
        val valTv = TextView(this).apply { text = "${fmtWeight(ex.weight)} kg"; setTextColor(textColor); setTextSize(TypedValue.COMPLEX_UNIT_SP, 26f); typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER; setPadding(0, dp(4), 0, dp(10)) }
        overlay.second.addView(valTv)

        val r1 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dp(6) } }
        r1.addView(makeBtn("-5 kg", surfaceColor, textColor, 12f, 8, 9, 4, borderColor) { ex.weight = max(0.0, ((ex.weight - 5.0) * 10).roundToInt() / 10.0); saveData(); valTv.text = "${fmtWeight(ex.weight)} kg" }.apply { layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { rightMargin = dp(6) } })
        r1.addView(makeBtn("+5 kg", accentColor, Color.parseColor("#1A1A1A"), 12f, 8, 9, 4) { ex.weight = ((ex.weight + 5.0) * 10).roundToInt() / 10.0; saveData(); valTv.text = "${fmtWeight(ex.weight)} kg" }.apply { layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f) })

        val r2 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dp(10) } }
        r2.addView(makeBtn("-2.5 kg", surfaceColor, textColor, 12f, 8, 9, 4, borderColor) { ex.weight = max(0.0, ((ex.weight - 2.5) * 10).roundToInt() / 10.0); saveData(); valTv.text = "${fmtWeight(ex.weight)} kg" }.apply { layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { rightMargin = dp(6) } })
        r2.addView(makeBtn("+2.5 kg", accentColor, Color.parseColor("#1A1A1A"), 12f, 8, 9, 4) { ex.weight = ((ex.weight + 2.5) * 10).roundToInt() / 10.0; saveData(); valTv.text = "${fmtWeight(ex.weight)} kg" }.apply { layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f) })

        overlay.second.addView(r1); overlay.second.addView(r2)
        overlay.second.addView(makeBtn("FATTO", accentColor, Color.parseColor("#1A1A1A"), 12f, 10, 9, 18) { rootContainer.removeView(overlay.first); renderFocus() })
        rootContainer.addView(overlay.first)
    }

    private fun showQuickRepsOverlay(ex: ExerciseItem) {
        val overlay = makeOverlayBox("MODIFICA RIPETIZIONI")
        val valTv = TextView(this).apply { text = "${ex.reps} rep"; setTextColor(textColor); setTextSize(TypedValue.COMPLEX_UNIT_SP, 26f); typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER; setPadding(0, dp(4), 0, dp(10)) }
        overlay.second.addView(valTv)

        val r1 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dp(6) } }
        r1.addView(makeBtn("-2 rep", surfaceColor, textColor, 12f, 8, 9, 4, borderColor) { ex.reps = max(1, ex.reps - 2); saveData(); valTv.text = "${ex.reps} rep" }.apply { layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { rightMargin = dp(6) } })
        r1.addView(makeBtn("+2 rep", accentColor, Color.parseColor("#1A1A1A"), 12f, 8, 9, 4) { ex.reps = min(99, ex.reps + 2); saveData(); valTv.text = "${ex.reps} rep" }.apply { layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f) })

        val r2 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dp(10) } }
        r2.addView(makeBtn("-1 rep", surfaceColor, textColor, 12f, 8, 9, 4, borderColor) { ex.reps = max(1, ex.reps - 1); saveData(); valTv.text = "${ex.reps} rep" }.apply { layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { rightMargin = dp(6) } })
        r2.addView(makeBtn("+1 rep", accentColor, Color.parseColor("#1A1A1A"), 12f, 8, 9, 4) { ex.reps = min(99, ex.reps + 1); saveData(); valTv.text = "${ex.reps} rep" }.apply { layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f) })

        overlay.second.addView(r1); overlay.second.addView(r2)
        overlay.second.addView(makeBtn("FATTO", accentColor, Color.parseColor("#1A1A1A"), 12f, 10, 9, 18) { rootContainer.removeView(overlay.first); renderFocus() })
        rootContainer.addView(overlay.first)
    }

    private fun showWorkoutPickerOverlay() {
        val overlay = makeOverlayBox("LE TUE SCHEDE")
        for (w in routines) {
            val active = w.id == activeRoutineId
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dp(6) } }
            row.addView(makeBtn("${w.name} (${w.exercises.size})", if (active) accentColor else surfaceColor, if (active) Color.parseColor("#1A1A1A") else textColor, 11f, 8, 8, 8, if (active) accentColor else borderColor) {
                activeRoutineId = w.id; saveData(); rootContainer.removeView(overlay.first)
                if (currentScreen == Screen.LIST) renderList() else renderHome()
            }.apply { layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f) })
            if (routines.size > 1) {
                row.addView(makeBtn("✕", surfaceColor, dangerColor, 11f, 8, 8, 9, dangerColor) {
                    routines.removeAll { it.id == w.id }; if (activeRoutineId == w.id) activeRoutineId = routines[0].id
                    saveData(); rootContainer.removeView(overlay.first); showWorkoutPickerOverlay()
                }.apply { layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { leftMargin = dp(5) } })
            }
            overlay.second.addView(row)
        }
        val btnRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(6) } }
        btnRow.addView(makeBtn("+ NUOVA", accentColor, Color.parseColor("#1A1A1A"), 11f, 8, 8, 8) { rootContainer.removeView(overlay.first); showNewWorkoutOverlay() }.apply { layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { rightMargin = dp(6) } })
        btnRow.addView(makeBtn("CHIUDI", surfaceColor, textColor, 11f, 8, 8, 8, borderColor) { rootContainer.removeView(overlay.first) }.apply { layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f) })
        overlay.second.addView(btnRow)
        rootContainer.addView(overlay.first)
    }

    private fun showNewWorkoutOverlay() {
        val overlay = makeOverlayBox("NUOVA SCHEDA")
        val input = EditText(this).apply {
            hint = "Es. Gambe & Glutei"; setTextColor(textColor); setHintTextColor(mutedColor); setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER; background = pillBg(surfaceColor, 8, borderColor, 1); setPadding(dp(8), dp(8), dp(8), dp(8))
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dp(8) }
        }
        overlay.second.addView(input)

        val chips = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dp(10) } }
        listOf("Upper", "Gambe", "Full Body").forEachIndexed { idx, txt ->
            chips.addView(makeBtn(txt, surfaceColor, mutedColor, 10f, 7, 5, 4, borderColor) { input.setText(txt) }.apply { layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { if (idx < 2) rightMargin = dp(4) } })
        }
        overlay.second.addView(chips)

        val btns = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        btns.addView(makeBtn("CREA", accentColor, Color.parseColor("#1A1A1A"), 11f, 8, 8, 8) {
            val name = input.text.toString().trim().ifEmpty { "Scheda ${routines.size + 1}" }
            val nw = WorkoutRoutine("w${System.currentTimeMillis()}", name, mutableListOf())
            routines.add(nw); activeRoutineId = nw.id; saveData(); rootContainer.removeView(overlay.first); renderList(); showToastMsg("Creata: $name")
        }.apply { layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { rightMargin = dp(6) } })
        btns.addView(makeBtn("ANNULLA", surfaceColor, textColor, 11f, 8, 8, 8, borderColor) { rootContainer.removeView(overlay.first) }.apply { layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f) })
        overlay.second.addView(btns)
        rootContainer.addView(overlay.first)
    }

    private fun makeOverlayBox(title: String): Pair<ScrollView, LinearLayout> {
        val backdrop = ScrollView(this).apply {
            setBackgroundColor(Color.parseColor("#F0101010")); isFillViewport = true
            layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setPadding(dp(14), dp(14), dp(14), dp(14)) }
        box.addView(TextView(this).apply { text = title; setTextColor(accentColor); setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f); typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER; setPadding(0, 0, 0, dp(8)) })
        backdrop.addView(box)
        return backdrop to box
    }

    private fun vibrateAlert() {
        try {
            val vib = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
            val pat = longArrayOf(0, 250, 100, 250, 100, 450)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) vib.vibrate(VibrationEffect.createWaveform(pat, -1))
            else @Suppress("DEPRECATION") vib.vibrate(pat, -1)
        } catch (_: Exception) {}
    }

    private fun playBeep() {
        try {
            val tg = ToneGenerator(AudioManager.STREAM_ALARM, 75)
            tg.startTone(ToneGenerator.TONE_PROP_BEEP2, 300)
            Handler(Looper.getMainLooper()).postDelayed({ try { tg.release() } catch (_: Exception) {} }, 400)
        } catch (_: Exception) {}
    }

    private fun setKeepScreenOn(enable: Boolean) {
        if (enable) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        else window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    // ==================== SQUARE RING ====================
    inner class SquareRingFrame(ctx: Context) : FrameLayout(ctx) {
        private var progress = 1f
        private val fullPath = Path()
        private val segPath = Path()
        private val pathMeasure = PathMeasure()
        private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; color = Color.parseColor("#2A2A2A"); strokeWidth = dp(4).toFloat() }
        private val activePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; color = accentColor; strokeWidth = dp(4).toFloat(); strokeCap = Paint.Cap.ROUND }

        fun setProgress(p: Float) { progress = p.coerceIn(0f, 1f); invalidate() }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            fullPath.reset()
            val pad = dp(3).toFloat(); val r = dp(18).toFloat(); val wf = w.toFloat(); val hf = h.toFloat()
            fullPath.moveTo(wf / 2f, pad); fullPath.lineTo(wf - pad - r, pad)
            fullPath.arcTo(RectF(wf - pad - 2 * r, pad, wf - pad, pad + 2 * r), -90f, 90f, false)
            fullPath.lineTo(wf - pad, hf - pad - r)
            fullPath.arcTo(RectF(wf - pad - 2 * r, hf - pad - 2 * r, wf - pad, hf - pad), 0f, 90f, false)
            fullPath.lineTo(pad + r, hf - pad)
            fullPath.arcTo(RectF(pad, hf - pad - 2 * r, pad + 2 * r, hf - pad), 90f, 90f, false)
            fullPath.lineTo(pad, pad + r)
            fullPath.arcTo(RectF(pad, pad, pad + 2 * r, pad + 2 * r), 180f, 90f, false)
            fullPath.lineTo(wf / 2f, pad)
            pathMeasure.setPath(fullPath, false)
        }

        override fun dispatchDraw(canvas: Canvas) {
            super.dispatchDraw(canvas)
            canvas.drawPath(fullPath, trackPaint)
            if (progress > 0f) {
                segPath.reset(); pathMeasure.getSegment(0f, pathMeasure.length * progress, segPath, true)
                segPath.rLineTo(0f, 0f); canvas.drawPath(segPath, activePaint)
            }
        }
    }

    // ==================== MACHINE ICONS ====================
    inner class MachineIconView(ctx: Context, private val iconId: String) : View(ctx) {
        private val pW = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; color = Color.WHITE; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND }
        private val pY = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; color = accentColor; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND }
        private val pYF = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL; color = accentColor }

        override fun onDraw(canvas: Canvas) {
            val s = min(width, height) / 48f
            pW.strokeWidth = 2.8f * s; pY.strokeWidth = 2.8f * s
            canvas.save(); canvas.translate((width - 48f * s) / 2f, (height - 48f * s) / 2f); canvas.scale(s, s)
            when (iconId) {
                "chest" -> { canvas.drawLine(12f, 14f, 12f, 34f, pW); canvas.drawLine(12f, 34f, 24f, 34f, pW); canvas.drawLine(18f, 34f, 18f, 40f, pW); canvas.drawLine(10f, 40f, 26f, 40f, pW); canvas.drawLine(24f, 14f, 24f, 24f, pY); canvas.drawLine(24f, 24f, 30f, 24f, pY); drawVectorArrow(canvas, 31f, 20f, 40f, 20f, pY, 4.5) }
                "lat" -> { canvas.drawLine(24f, 7f, 24f, 14f, pW); canvas.drawLine(13f, 36f, 35f, 36f, pW); canvas.drawLine(24f, 36f, 24f, 41f, pW); canvas.drawLine(16f, 30f, 32f, 30f, pW); canvas.drawLine(10f, 17f, 16f, 14f, pY); canvas.drawLine(16f, 14f, 32f, 14f, pY); canvas.drawLine(32f, 14f, 38f, 17f, pY); drawVectorArrow(canvas, 24f, 19f, 24f, 27f, pY, 4.5) }
                "pulley" -> { canvas.drawLine(10f, 34f, 22f, 34f, pW); canvas.drawLine(16f, 34f, 16f, 40f, pW); canvas.drawLine(36f, 16f, 36f, 36f, pW); canvas.drawLine(12f, 18f, 14f, 34f, pW); canvas.drawLine(27f, 24f, 36f, 24f, pW); drawVectorArrow(canvas, 25f, 24f, 16f, 24f, pY, 4.5) }
                "verticalrow" -> { canvas.drawLine(15f, 34f, 29f, 34f, pW); canvas.drawLine(22f, 34f, 22f, 40f, pW); canvas.drawLine(31f, 14f, 31f, 34f, pW); canvas.drawLine(23f, 16f, 23f, 26f, pY); drawVectorArrow(canvas, 28f, 22f, 14f, 22f, pY, 4.5) }
                "shoulder" -> { canvas.drawLine(16f, 18f, 16f, 34f, pW); canvas.drawLine(16f, 34f, 32f, 34f, pW); canvas.drawLine(24f, 34f, 24f, 40f, pW); canvas.drawLine(14f, 20f, 20f, 20f, pY); canvas.drawLine(28f, 20f, 34f, 20f, pY); drawVectorArrow(canvas, 24f, 22f, 24f, 10f, pY, 4.5) }
                "delts" -> { canvas.drawLine(24f, 14f, 24f, 36f, pW); canvas.drawLine(16f, 36f, 32f, 36f, pW); canvas.drawLine(19f, 20f, 10f, 14f, pY); canvas.drawLine(29f, 20f, 38f, 14f, pY) }
                "pectoral" -> { canvas.drawLine(24f, 12f, 24f, 36f, pW); canvas.drawLine(16f, 36f, 32f, 36f, pW); canvas.drawLine(10f, 16f, 10f, 26f, pY); canvas.drawLine(38f, 16f, 38f, 26f, pY); drawVectorArrow(canvas, 13f, 21f, 20f, 21f, pY, 4.0); drawVectorArrow(canvas, 35f, 21f, 28f, 21f, pY, 4.0) }
                "armcurl" -> { canvas.drawLine(12f, 36f, 34f, 36f, pW); canvas.drawLine(18f, 36f, 26f, 20f, pW); canvas.drawCircle(26f, 24f, 3f, pYF); drawVectorArrow(canvas, 34f, 30f, 23f, 14f, pY, 4.5) }
                "armext" -> { canvas.drawLine(14f, 14f, 14f, 34f, pW); canvas.drawLine(14f, 34f, 28f, 34f, pW); canvas.drawLine(20f, 34f, 20f, 40f, pW); canvas.drawCircle(24f, 20f, 2.5f, pYF); drawVectorArrow(canvas, 24f, 20f, 34f, 28f, pY, 4.5) }
                "legpress" -> { canvas.drawLine(9f, 22f, 16f, 35f, pW); canvas.drawLine(16f, 35f, 25f, 35f, pW); canvas.drawLine(10f, 40f, 38f, 40f, pW); canvas.drawLine(34f, 11f, 40f, 23f, pY); drawVectorArrow(canvas, 22f, 28f, 31f, 19f, pY, 4.5) }
                "legext" -> { canvas.drawLine(12f, 16f, 14f, 30f, pW); canvas.drawLine(14f, 30f, 28f, 30f, pW); canvas.drawLine(16f, 30f, 16f, 40f, pW); canvas.drawLine(28f, 30f, 33f, 38f, pW); canvas.drawCircle(28f, 30f, 2.8f, pYF); drawVectorArrow(canvas, 33f, 34f, 38f, 20f, pY, 4.5) }
                "legcurl" -> { canvas.drawLine(10f, 26f, 32f, 26f, pW); canvas.drawLine(14f, 26f, 14f, 38f, pW); canvas.drawLine(28f, 26f, 28f, 38f, pW); canvas.drawCircle(30f, 26f, 2.8f, pYF); drawVectorArrow(canvas, 38f, 28f, 28f, 39f, pY, 4.5) }
                "abductor" -> { canvas.drawLine(15f, 33f, 33f, 33f, pW); canvas.drawLine(24f, 33f, 24f, 40f, pW); canvas.drawLine(18f, 40f, 30f, 40f, pW); canvas.drawLine(19f, 16f, 21f, 28f, pW); canvas.drawLine(29f, 16f, 27f, 28f, pW); drawVectorArrow(canvas, 16f, 21f, 9f, 21f, pY, 4.0); drawVectorArrow(canvas, 32f, 21f, 39f, 21f, pY, 4.0) }
                "adductor" -> { canvas.drawLine(15f, 33f, 33f, 33f, pW); canvas.drawLine(24f, 33f, 24f, 40f, pW); canvas.drawLine(18f, 40f, 30f, 40f, pW); canvas.drawLine(11f, 16f, 16f, 28f, pW); canvas.drawLine(37f, 16f, 32f, 28f, pW); drawVectorArrow(canvas, 14f, 21f, 21f, 21f, pY, 4.0); drawVectorArrow(canvas, 34f, 21f, 27f, 21f, pY, 4.0) }
                "glute" -> { canvas.drawLine(12f, 22f, 26f, 22f, pW); canvas.drawLine(26f, 22f, 20f, 38f, pW); canvas.drawLine(14f, 22f, 10f, 38f, pW); canvas.drawLine(26f, 24f, 38f, 18f, pY); drawVectorArrow(canvas, 31f, 29f, 40f, 22f, pY, 4.5) }
                "abs" -> { canvas.drawLine(12f, 18f, 16f, 34f, pW); canvas.drawLine(16f, 34f, 28f, 34f, pW); canvas.drawLine(18f, 34f, 18f, 40f, pW); canvas.drawLine(18f, 40f, 26f, 40f, pW); canvas.drawRect(25f, 13f, 31f, 22f, pY); drawVectorArrow(canvas, 24f, 15f, 34f, 26f, pY, 4.5) }
                "lowerback" -> { canvas.drawLine(14f, 34f, 30f, 34f, pW); canvas.drawLine(22f, 34f, 22f, 40f, pW); canvas.drawLine(22f, 34f, 28f, 18f, pW); drawVectorArrow(canvas, 26f, 22f, 12f, 16f, pY, 4.5) }
                "bike" -> { canvas.drawCircle(15f, 33f, 7f, pW); canvas.drawCircle(15f, 33f, 2.5f, pYF); canvas.drawLine(15f, 33f, 24f, 22f, pW); canvas.drawLine(24f, 22f, 32f, 34f, pW); canvas.drawLine(32f, 34f, 15f, 33f, pW); canvas.drawLine(32f, 34f, 35f, 16f, pW); canvas.drawLine(35f, 16f, 39f, 16f, pW) }
                "treadmill" -> { canvas.drawLine(8f, 36f, 38f, 30f, pW); canvas.drawLine(35f, 31f, 39f, 14f, pW); canvas.drawLine(39f, 14f, 34f, 14f, pW); canvas.drawCircle(21f, 14f, 3f, pYF); canvas.drawLine(21f, 18f, 19f, 25f, pY); canvas.drawLine(19f, 25f, 25f, 29f, pY); canvas.drawLine(19f, 25f, 14f, 31f, pY) }
                "body_upper" -> { canvas.drawCircle(24f, 10f, 3.5f, pYF); canvas.drawLine(11f, 20f, 17f, 17f, pY); canvas.drawLine(17f, 17f, 31f, 17f, pY); canvas.drawLine(31f, 17f, 37f, 20f, pY); canvas.drawRect(18f, 17f, 30f, 31f, pW); canvas.drawLine(20f, 31f, 20f, 40f, pW); canvas.drawLine(28f, 31f, 28f, 40f, pW) }
                "body_legs" -> { canvas.drawRect(18f, 10f, 30f, 22f, pW); canvas.drawLine(18f, 22f, 14f, 32f, pY); canvas.drawLine(14f, 32f, 18f, 40f, pY); canvas.drawLine(30f, 22f, 34f, 32f, pY); canvas.drawLine(34f, 32f, 30f, 40f, pY) }
                "body_core" -> { canvas.drawCircle(11f, 24f, 3f, pYF); canvas.drawLine(15f, 26f, 38f, 33f, pY); canvas.drawLine(17f, 27f, 17f, 35f, pW); canvas.drawLine(17f, 35f, 21f, 35f, pW); canvas.drawLine(8f, 37f, 40f, 37f, pW) }
            }
            canvas.restore()
        }
    }

    // ==================== POSTURE DIAGRAMS ====================
    inner class PostureDiagramView(ctx: Context, private val iconId: String) : View(ctx) {
        private val pM = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; color = Color.parseColor("#666666"); strokeWidth = 3f; strokeCap = Paint.Cap.ROUND }
        private val pW = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; color = Color.WHITE; strokeWidth = 3.2f; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND }
        private val pY = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; color = accentColor; strokeWidth = 2.8f; strokeCap = Paint.Cap.ROUND }
        private val pYF = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL; color = accentColor }
        private val pTxt = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accentColor; textSize = 9.5f; typeface = Typeface.DEFAULT_BOLD }

        override fun onDraw(canvas: Canvas) {
            val s = min(width / 260f, height / 112f)
            canvas.save(); canvas.translate((width - 260f * s) / 2f, (height - 112f * s) / 2f); canvas.scale(s, s)
            when (iconId) {
                "chest", "shoulder", "armext" -> {
                    canvas.drawLine(78f, 28f, 78f, 86f, pM); canvas.drawLine(78f, 86f, 126f, 86f, pM)
                    canvas.drawCircle(94f, 30f, 8f, pW); canvas.drawLine(92f, 39f, 92f, 74f, pW); canvas.drawLine(92f, 74f, 124f, 76f, pW); canvas.drawLine(124f, 76f, 128f, 96f, pW)
                    when (iconId) {
                        "shoulder" -> { canvas.drawLine(94f, 48f, 114f, 36f, pW); canvas.drawCircle(114f, 36f, 4.5f, pYF); drawVectorArrow(canvas, 134f, 46f, 134f, 18f, pY, 6.5); canvas.drawText("SPINTA VERTICALE", 145f, 32f, pTxt) }
                        "armext" -> { canvas.drawLine(96f, 46f, 102f, 64f, pW); canvas.drawLine(102f, 64f, 126f, 54f, pW); canvas.drawCircle(102f, 64f, 4f, pYF); drawVectorArrow(canvas, 134f, 48f, 134f, 76f, pY, 6.5); canvas.drawText("GOMITI AI FIANCHI", 145f, 52f, pTxt) }
                        else -> { canvas.drawLine(94f, 49f, 138f, 49f, pW); canvas.drawCircle(138f, 49f, 4.5f, pYF); drawVectorArrow(canvas, 148f, 49f, 184f, 49f, pY, 6.5); canvas.drawText("MANIGLIE METÀ PETTO", 142f, 34f, pTxt) }
                    }
                }
                "lat" -> {
                    canvas.drawLine(95f, 88f, 165f, 88f, pM); canvas.drawLine(130f, 88f, 130f, 102f, pM); canvas.drawCircle(130f, 34f, 8f, pW)
                    canvas.drawLine(130f, 43f, 135f, 68f, pW); canvas.drawLine(135f, 68f, 112f, 72f, pW); canvas.drawLine(131f, 46f, 106f, 24f, pW); canvas.drawLine(131f, 46f, 156f, 24f, pW)
                    canvas.drawLine(86f, 22f, 174f, 22f, pY); canvas.drawCircle(106f, 22f, 4f, pYF); canvas.drawCircle(156f, 22f, 4f, pYF)
                    drawVectorArrow(canvas, 130f, 26f, 130f, 42f, pY, 6.5); canvas.drawText("PRESA AMPIA • PETTO ALTO", 16f, 18f, pTxt)
                }
                "pulley", "verticalrow" -> {
                    canvas.drawLine(40f, 86f, 135f, 86f, pM); canvas.drawLine(185f, 30f, 185f, 96f, pM); canvas.drawCircle(84f, 30f, 8f, pW)
                    canvas.drawLine(84f, 39f, 84f, 76f, pW); canvas.drawLine(84f, 76f, 136f, 76f, pW); canvas.drawLine(136f, 76f, 178f, 64f, pW)
                    canvas.drawLine(84f, 48f, 128f, 56f, pW); canvas.drawCircle(128f, 56f, 4.5f, pYF); drawVectorArrow(canvas, 126f, 42f, 92f, 42f, pY, 6.5)
                    canvas.drawText("SCHIENA DRITTA • TIRA INDIETRO", 18f, 16f, pTxt)
                }
                "delts", "pectoral", "abductor", "adductor" -> {
                    canvas.drawLine(104f, 82f, 156f, 82f, pM); canvas.drawLine(130f, 82f, 130f, 100f, pM); canvas.drawCircle(130f, 22f, 7.5f, pW)
                    canvas.drawRect(118f, 34f, 142f, 70f, pW); canvas.drawLine(118f, 42f, 86f, 42f, pW); canvas.drawLine(142f, 42f, 174f, 42f, pW)
                    canvas.drawCircle(86f, 42f, 4.5f, pYF); canvas.drawCircle(174f, 42f, 4.5f, pYF)
                    if (iconId == "pectoral" || iconId == "adductor") {
                        drawVectorArrow(canvas, 92f, 28f, 120f, 28f, pY, 6.5); drawVectorArrow(canvas, 168f, 28f, 140f, 28f, pY, 6.5); canvas.drawText("CHIUDI AL CENTRO", 82f, 14f, pTxt)
                    } else {
                        drawVectorArrow(canvas, 82f, 28f, 54f, 28f, pY, 6.5); drawVectorArrow(canvas, 178f, 28f, 206f, 28f, pY, 6.5); canvas.drawText("APRI IN CONTROLLO", 80f, 14f, pTxt)
                    }
                }
                "legpress" -> {
                    canvas.drawLine(58f, 56f, 88f, 86f, pM); canvas.drawLine(88f, 86f, 122f, 86f, pM); canvas.drawLine(174f, 26f, 194f, 64f, pM)
                    canvas.drawCircle(68f, 40f, 8f, pW); canvas.drawLine(74f, 49f, 94f, 78f, pW); canvas.drawLine(94f, 78f, 134f, 58f, pW); canvas.drawLine(134f, 58f, 178f, 44f, pW)
                    canvas.drawCircle(94f, 78f, 4f, pYF); canvas.drawCircle(178f, 44f, 4.5f, pYF); drawVectorArrow(canvas, 142f, 36f, 168f, 24f, pY, 6.5)
                    canvas.drawText("NO BLOCCO GINOCCHIA", 128f, 96f, pTxt)
                }
                "legext", "legcurl" -> {
                    canvas.drawLine(74f, 28f, 74f, 74f, pM); canvas.drawLine(74f, 74f, 132f, 74f, pM); canvas.drawCircle(90f, 26f, 8f, pW)
                    canvas.drawLine(88f, 35f, 88f, 68f, pW); canvas.drawLine(88f, 68f, 136f, 68f, pW); canvas.drawCircle(136f, 68f, 5f, pYF)
                    if (iconId == "legext") {
                        canvas.drawLine(136f, 68f, 142f, 96f, pW); drawVectorArrow(canvas, 152f, 90f, 174f, 62f, pY, 6.5); canvas.drawText("GINOCCHIO AL PERNO", 136f, 36f, pTxt)
                    } else {
                        canvas.drawLine(136f, 68f, 176f, 68f, pW); drawVectorArrow(canvas, 174f, 78f, 138f, 98f, pY, 6.5); canvas.drawText("TALLONI AI GLUTEI", 138f, 36f, pTxt)
                    }
                }
                "armcurl" -> {
                    canvas.drawLine(74f, 88f, 142f, 88f, pM); canvas.drawLine(124f, 44f, 142f, 74f, pM); canvas.drawCircle(94f, 30f, 8f, pW)
                    canvas.drawLine(94f, 39f, 94f, 76f, pW); canvas.drawLine(94f, 76f, 124f, 78f, pW); canvas.drawLine(98f, 46f, 136f, 58f, pW); canvas.drawLine(136f, 58f, 158f, 44f, pW)
                    canvas.drawCircle(136f, 58f, 4.5f, pYF); drawVectorArrow(canvas, 168f, 52f, 144f, 24f, pY, 6.5); canvas.drawText("GOMITI FISSI AL PAD", 142f, 82f, pTxt)
                }
                "glute" -> {
                    canvas.drawLine(72f, 48f, 106f, 48f, pM); canvas.drawLine(88f, 48f, 88f, 94f, pM); canvas.drawCircle(114f, 30f, 7.5f, pW)
                    canvas.drawLine(110f, 38f, 94f, 62f, pW); canvas.drawLine(94f, 62f, 92f, 94f, pW); canvas.drawLine(94f, 62f, 148f, 58f, pY); canvas.drawCircle(148f, 58f, 4.5f, pYF)
                    drawVectorArrow(canvas, 138f, 74f, 168f, 56f, pY, 6.5); canvas.drawText("SPINGI COL TALLONE", 132f, 32f, pTxt)
                }
                "abs", "lowerback" -> {
                    canvas.drawLine(84f, 78f, 136f, 78f, pM); canvas.drawLine(108f, 78f, 108f, 96f, pM); canvas.drawCircle(104f, 30f, 8f, pW)
                    canvas.drawLine(104f, 39f, 108f, 76f, pW); canvas.drawLine(108f, 76f, 138f, 78f, pW); canvas.drawLine(138f, 78f, 142f, 96f, pW)
                    if (iconId == "abs") { drawVectorArrow(canvas, 122f, 24f, 148f, 52f, pY, 6.5); canvas.drawText("ARROTOLA & ESPIRA", 140f, 28f, pTxt) }
                    else { drawVectorArrow(canvas, 100f, 28f, 74f, 52f, pY, 6.5); canvas.drawText("ESTENDI FINO A 180°", 136f, 30f, pTxt) }
                }
                "bike", "treadmill" -> {
                    canvas.drawLine(50f, 92f, 210f, 92f, pM); canvas.drawCircle(120f, 26f, 7.5f, pW)
                    canvas.drawLine(118f, 35f, 114f, 62f, pW); canvas.drawLine(114f, 62f, 128f, 84f, pW); canvas.drawCircle(120f, 66f, 4.5f, pYF)
                    drawVectorArrow(canvas, 134f, 26f, 164f, 26f, pY, 6.5)
                    canvas.drawText(if (iconId == "bike") "GINOCCHIO ~25°" else "SGUARDO AVANTI 1-2%", 142f, 54f, pTxt)
                }
                "body_upper", "body_core" -> {
                    canvas.drawLine(36f, 88f, 224f, 88f, pM); canvas.drawCircle(78f, 48f, 7f, pW); canvas.drawLine(86f, 52f, 194f, 74f, pW)
                    canvas.drawLine(90f, 54f, 90f, 86f, pW); canvas.drawCircle(90f, 86f, 4.5f, pYF); canvas.drawLine(76f, 40f, 198f, 62f, pY)
                    canvas.drawText("LINEA RETTA TESTA-TALLONI", 82f, 28f, pTxt)
                }
                else -> {
                    canvas.drawLine(50f, 92f, 210f, 92f, pM); canvas.drawCircle(118f, 24f, 7.5f, pW)
                    canvas.drawLine(114f, 32f, 96f, 62f, pW); canvas.drawLine(96f, 62f, 134f, 64f, pW); canvas.drawLine(134f, 64f, 126f, 90f, pW)
                    canvas.drawCircle(96f, 62f, 4f, pYF); canvas.drawCircle(134f, 64f, 4f, pYF)
                    drawVectorArrow(canvas, 164f, 40f, 164f, 78f, pY, 6.5); canvas.drawText("FIANCHI INDIETRO", 14f, 44f, pTxt)
                }
            }
            canvas.restore()
        }
    }
}
