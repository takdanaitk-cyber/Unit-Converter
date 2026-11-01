package com.engunits.converter

import java.util.Locale
import kotlin.math.abs

/** ผลของการแยกคำสั่งเสียง: หมวด, ค่าที่พูด (ถ้ามี), หน่วยต้นทาง, หน่วยปลายทาง (ถ้าพูดถึง) */
class VoiceResult(val category: Int, val value: Double?, val from: Int, val to: Int?)

/**
 * แยกคำสั่งเสียงภาษาไทย/อังกฤษ เช่น
 *   "แปลง 100 CFM เป็น ลูกบาศก์เมตรต่อชั่วโมง"
 *   "convert 5 bar to psi"
 *   "ติดลบ 40 องศาเซลเซียส เป็น ฟาเรนไฮต์"
 * หน่วยหาจาก UnitDef.aliases โดยเลือก alias ที่ยาวที่สุดที่ตรง
 */
object VoiceParser {
    private class Hit(val cat: Int, val unit: Int, val score: Int)

    private val numRegex =
        Regex("((?:ติดลบ|ลบ|minus|negative)\\s*)?(?<![a-z\\d])(-?\\d+(?:\\.\\d+)?)")
    private val sepRegex =
        Regex("เป็น|เท่ากับ|(?<![a-z0-9])(?:to|into|equals|equal)(?![a-z0-9])|=")
    private val noStrip = setOf("celsius")
    private val regexCache = HashMap<String, Regex>()

    fun parse(raw: String, currentCategory: Int, currentFrom: Int): VoiceResult? {
        var s = clean(raw)

        var value: Double? = null
        val m = numRegex.find(s)
        if (m != null) {
            var v = m.groupValues[2].toDoubleOrNull()
            if (v != null && m.groupValues[1].isNotEmpty()) v = -abs(v)
            value = v
            s = s.substring(0, m.range.first) + " " + s.substring(m.range.last + 1)
        }

        val sp = spaced(s)
        val sep = sepRegex.find(sp)
        val left = if (sep != null) sp.substring(0, sep.range.first) else sp
        val right = if (sep != null) sp.substring(sep.range.last + 1) else null

        val l = hits(left)
        val r = if (right != null) hits(right) else emptyList()

        // ต้นทาง+ปลายทางอยู่หมวดเดียวกัน คะแนนรวมสูงสุดชนะ
        var bestScore = -1
        var pair: Pair<Hit, Hit>? = null
        for (a in l) for (b in r) {
            if (a.cat == b.cat && a.score + b.score > bestScore) {
                bestScore = a.score + b.score
                pair = a to b
            }
        }
        if (pair != null) {
            return VoiceResult(pair.first.cat, value, pair.first.unit, pair.second.unit)
        }
        val bestLeft = l.maxByOrNull { it.score }
        if (bestLeft != null) {
            return VoiceResult(bestLeft.cat, value, bestLeft.unit, null)
        }
        // พูดแต่ปลายทาง เช่น "เป็นนิ้ว": ใช้หน่วยต้นทางปัจจุบันถ้าอยู่หมวดเดียวกัน
        val bestRight = r.filter { it.cat == currentCategory }.maxByOrNull { it.score }
        if (bestRight != null) {
            return VoiceResult(currentCategory, value, currentFrom, bestRight.unit)
        }
        return null
    }

    private fun clean(raw: String): String {
        val thaiDigits = "๐๑๒๓๔๕๖๗๘๙"
        var s = raw.lowercase(Locale.ROOT)
        s = s.map { c ->
            val i = thaiDigits.indexOf(c)
            if (i >= 0) '0' + i else c
        }.joinToString("")
        s = s.replace('²', '2').replace('³', '3').replace('°', ' ').replace('º', ' ')
        s = s.replace(Regex("(?<=\\d),(?=\\d{3})"), "")
        return s
    }

    private fun spaced(s: String): String {
        var t = s.replace("/", " per ").replace("-", " ").replace(".", "")
        t = t.replace(Regex("[^\\p{L}\\p{M}\\p{N}\\s=]"), " ")
        t = t.replace("feet", "foot").replace("inches", "inch")
        t = t.split(Regex("\\s+")).joinToString(" ") { w ->
            if (w.length > 3 && w !in noStrip && w.all { it in 'a'..'z' } && w.endsWith("s")) w.dropLast(1) else w
        }
        return t.trim().replace(Regex("\\s+"), " ")
    }

    private fun latinRegex(alias: String): Regex = regexCache.getOrPut(alias) {
        Regex(
            "(?<![a-z0-9])" +
                alias.split(" ").joinToString("\\s+") { Regex.escape(it) } +
                "(?![a-z0-9])"
        )
    }

    private fun hits(fragment: String): List<Hit> {
        val comp = fragment.replace(" per ", "ต่อ").replace(" ", "")
        val out = ArrayList<Hit>()
        Categories.forEachIndexed { ci, cat ->
            cat.units.forEachIndexed { ui, u ->
                var best = 0
                for (a in u.aliases) {
                    val thai = a.any { it in '฀'..'๿' }
                    val ok = if (thai) comp.contains(a.replace(" ", ""))
                    else latinRegex(a).containsMatchIn(fragment)
                    if (ok) best = maxOf(best, a.replace(" ", "").length)
                }
                if (best > 0) out.add(Hit(ci, ui, best))
            }
        }
        return out
    }
}
