package com.engunits.converter

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme { App() }
        }
    }
}

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val ctx = LocalContext.current
    val scheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        dark -> darkColorScheme()
        else -> lightColorScheme()
    }
    MaterialTheme(colorScheme = scheme, content = content)
}

private fun <T> List<T>.with(i: Int, v: T): List<T> = toMutableList().also { it[i] = v }

/** ไอคอนและสีประจำหมวด เรียงตามลำดับเดียวกับ Categories ใน Units.kt */
private val CategoryIcons = listOf(
    R.drawable.ic_cat_temperature,
    R.drawable.ic_cat_pressure,
    R.drawable.ic_cat_flow,
    R.drawable.ic_cat_speed,
    R.drawable.ic_cat_weight,
    R.drawable.ic_cat_time,
    R.drawable.ic_cat_length,
    R.drawable.ic_cat_area,
    R.drawable.ic_cat_volume,
    R.drawable.ic_cat_energy,
)

private val CategoryColors = listOf(
    Color(0xFFE53935), // อุณหภูมิ
    Color(0xFF8E24AA), // ความดัน
    Color(0xFF039BE5), // อัตราการไหล
    Color(0xFFFB8C00), // ความเร็ว
    Color(0xFF6D4C41), // น้ำหนัก
    Color(0xFF3949AB), // เวลา
    Color(0xFF00897B), // ระยะ
    Color(0xFF7CB342), // พื้นที่
    Color(0xFF00ACC1), // ปริมาตร
    Color(0xFFFDD835), // พลังงาน
)

@Composable
fun CategoryBadge(index: Int, selected: Boolean) {
    val color = CategoryColors.getOrElse(index) { Color(0xFF607D8B) }
    val icon = CategoryIcons.getOrNull(index) ?: return
    Box(
        modifier = Modifier
            .size(if (selected) 40.dp else 34.dp)
            .background(if (selected) color else color.copy(alpha = 0.75f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painterResource(icon),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(if (selected) 24.dp else 20.dp),
        )
    }
}

private const val LANG_TH = "th-TH"
private const val LANG_EN = "en-US"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val context = LocalContext.current
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var texts by rememberSaveable { mutableStateOf(List(Categories.size) { "1" }) }
    var froms by rememberSaveable { mutableStateOf(List(Categories.size) { 0 }) }
    var targets by rememberSaveable { mutableStateOf(List(Categories.size) { -1 }) }
    var lang by rememberSaveable { mutableStateOf(LANG_TH) }
    var voiceMsg by rememberSaveable { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        if (res.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
        val heard = res.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).orEmpty()
        if (heard.isEmpty()) {
            voiceMsg = "ไม่ได้ยินเสียง ลองใหม่อีกครั้ง"
            return@rememberLauncherForActivityResult
        }
        var applied = false
        for (h in heard) {
            val r = VoiceParser.parse(h, tab, froms[tab]) ?: continue
            tab = r.category
            r.value?.let { texts = texts.with(r.category, formatNumber(it)) }
            froms = froms.with(r.category, r.from)
            targets = targets.with(r.category, r.to ?: -1)
            voiceMsg = "ได้ยิน: \"$h\""
            applied = true
            break
        }
        if (!applied) {
            voiceMsg = "ไม่เข้าใจคำสั่ง \"${heard[0]}\" ลองพูดเช่น \"แปลง 100 CFM เป็น ลูกบาศก์เมตรต่อชั่วโมง\""
        }
    }

    fun startVoice() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                if (lang == LANG_TH) "พูดคำสั่ง เช่น แปลง 100 CFM เป็น ลูกบาศก์เมตรต่อชั่วโมง"
                else "Say e.g. convert 100 CFM to cubic meter per hour"
            )
        }
        try {
            launcher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "เครื่องนี้ไม่มีบริการรู้จำเสียง (ต้องมีแอป Google)", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Unit Converter") },
                actions = {
                    TextButton(onClick = { lang = if (lang == LANG_TH) LANG_EN else LANG_TH }) {
                        Text(if (lang == LANG_TH) "TH" else "EN")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { startVoice() }) {
                Icon(painterResource(R.drawable.ic_mic), contentDescription = "สั่งงานด้วยเสียง")
            }
        },
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            ScrollableTabRow(selectedTabIndex = tab, edgePadding = 8.dp) {
                Categories.forEachIndexed { i, c ->
                    Tab(
                        selected = tab == i,
                        onClick = { tab = i },
                        icon = { CategoryBadge(i, tab == i) },
                        text = { Text(c.title, maxLines = 1) },
                    )
                }
            }
            ConverterScreen(
                cat = Categories[tab],
                text = texts[tab],
                from = froms[tab],
                target = targets[tab],
                voiceMsg = voiceMsg,
                onText = { texts = texts.with(tab, it) },
                onFrom = { froms = froms.with(tab, it) },
                onTarget = { targets = targets.with(tab, it) },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ConverterScreen(
    cat: Category,
    text: String,
    from: Int,
    target: Int,
    voiceMsg: String?,
    onText: (String) -> Unit,
    onFrom: (Int) -> Unit,
    onTarget: (Int) -> Unit,
) {
    val context = LocalContext.current
    var menuOpen by rememberSaveable(cat.title) { mutableStateOf(false) }

    val value = text.toDoubleOrNull()
    val base = value?.let { cat.units[from].toBase(it) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = text,
                onValueChange = { s -> onText(s.filter { it.isDigit() || it in ".-eE" }) },
                label = { Text("ค่าที่ต้องการแปลง") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            OutlinedButton(onClick = {
                onText(if (text.startsWith("-")) text.drop(1) else "-$text")
            }) { Text("±") }
            Spacer(Modifier.width(8.dp))
            Box {
                OutlinedButton(onClick = { menuOpen = true }) { Text(cat.units[from].symbol) }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    cat.units.forEachIndexed { i, u ->
                        DropdownMenuItem(
                            text = { Text("${u.name} (${u.symbol})") },
                            onClick = { onFrom(i); menuOpen = false },
                        )
                    }
                }
            }
        }

        Text(
            voiceMsg ?: "แตะไมค์เพื่อสั่งด้วยเสียง · แตะแถวเพื่อใช้เป็นหน่วยต้นทาง · กดค้างเพื่อคัดลอก",
            style = MaterialTheme.typography.bodySmall,
            color = if (voiceMsg != null) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
        )

        if (target in cat.units.indices && value != null && base != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            ) {
                Text(
                    "${formatNumber(value)} ${cat.units[from].symbol} = " +
                        "${formatNumber(cat.units[target].fromBase(base))} ${cat.units[target].symbol}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(12.dp),
                )
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 96.dp),
        ) {
            itemsIndexed(cat.units) { i, u ->
                val result = base?.let { formatNumber(u.fromBase(it)) } ?: "—"
                val container = when {
                    i == from -> MaterialTheme.colorScheme.primaryContainer
                    i == target -> MaterialTheme.colorScheme.tertiaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = container),
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {
                                if (result != "—") onText(result)
                                onFrom(i)
                                onTarget(-1)
                            },
                            onLongClick = {
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cm.setPrimaryClip(ClipData.newPlainText("value", result))
                                Toast.makeText(context, "คัดลอกแล้ว: $result", Toast.LENGTH_SHORT).show()
                            },
                        ),
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(u.name, fontWeight = FontWeight.Medium)
                            Text(u.symbol, style = MaterialTheme.typography.bodySmall)
                        }
                        Text(result, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
