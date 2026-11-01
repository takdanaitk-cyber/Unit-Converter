package com.engunits.converter

import java.math.BigDecimal
import java.math.MathContext
import java.util.Locale
import kotlin.math.abs

/**
 * หน่วยหนึ่งหน่วย: แปลงเข้า/ออกจากหน่วยฐานของหมวดนั้น
 * aliases = คำที่ใช้เรียกหน่วยนี้ด้วยเสียง (ไทย/อังกฤษ) ใช้โดย VoiceParser
 * กฎการเขียน alias: ตัวพิมพ์เล็ก, ภาษาไทยเขียนติดกัน, "ต่อ" ในภาษาไทย, " per " ในภาษาอังกฤษ,
 * ไม่ใส่จุด และเขียนยกกำลังเป็นตัวเลข เช่น m3, cm2
 */
class UnitDef(
    val name: String,
    val symbol: String,
    val toBase: (Double) -> Double,
    val fromBase: (Double) -> Double,
    val aliases: List<String> = emptyList(),
)

class Category(val title: String, val units: List<UnitDef>)

/** หน่วยเชิงเส้น: ค่าฐาน = ค่า × factor */
private fun lin(name: String, symbol: String, factor: Double, vararg aliases: String) =
    UnitDef(name, symbol, { it * factor }, { it / factor }, aliases.toList())

val Categories: List<Category> = listOf(
    // ฐาน: องศาเซลเซียส
    Category(
        "อุณหภูมิ", listOf(
            UnitDef("องศาเซลเซียส", "°C", { it }, { it },
                listOf("เซลเซียส", "celsius", "centigrade", "degree c", "deg c", "องศาซี")),
            UnitDef("องศาฟาเรนไฮต์", "°F", { (it - 32.0) / 1.8 }, { it * 1.8 + 32.0 },
                listOf("ฟาเรนไฮต์", "ฟาเรนไฮท์", "ฟาเรนไฮน์", "fahrenheit", "degree f", "deg f", "องศาเอฟ")),
            UnitDef("เคลวิน", "K", { it - 273.15 }, { it + 273.15 },
                listOf("เคลวิน", "kelvin")),
            UnitDef("องศาแรงคิน", "°R", { it / 1.8 - 273.15 }, { (it + 273.15) * 1.8 },
                listOf("แรงคิน", "rankine")),
        )
    ),
    // ฐาน: ปาสคาล
    Category(
        "ความดัน", listOf(
            lin("ปาสคาล", "Pa", 1.0, "ปาสคาล", "ปาสกาล", "pascal", "pa"),
            lin("กิโลปาสคาล", "kPa", 1000.0, "กิโลปาสคาล", "กิโลปาสกาล", "kilopascal", "kpa"),
            lin("เมกะปาสคาล", "MPa", 1_000_000.0, "เมกะปาสคาล", "เมกะปาสกาล", "megapascal", "mpa"),
            lin("บาร์", "bar", 100_000.0, "บาร์", "bar"),
            lin("มิลลิบาร์", "mbar", 100.0, "มิลลิบาร์", "millibar", "mbar"),
            lin("บรรยากาศ", "atm", 101_325.0, "บรรยากาศ", "แอตมอสเฟียร์", "atmosphere", "atm"),
            lin("ปอนด์/ตร.นิ้ว", "psi", 6894.757293168, "psi", "พีเอสไอ", "ปอนด์ต่อตารางนิ้ว", "pound per square inch"),
            lin("นิ้วน้ำ (4°C)", "inH₂O", 249.08891, "นิ้วน้ำ", "inch of water", "inch water", "inh2o", "inwc"),
            lin("มม.น้ำ", "mmH₂O", 9.80665, "มิลลิเมตรน้ำ", "มมน้ำ", "millimeter of water", "mmh2o", "mmwg"),
            lin("มม.ปรอท", "mmHg", 133.322387415, "มิลลิเมตรปรอท", "มมปรอท", "millimeter of mercury", "mmhg"),
            lin("นิ้วปรอท", "inHg", 3386.389, "นิ้วปรอท", "inch of mercury", "inhg"),
            lin("กก.แรง/ตร.ซม.", "kgf/cm²", 98_066.5, "กิโลกรัมต่อตารางเซนติเมตร", "กิโลกรัมแรงต่อตารางเซนติเมตร",
                "กกต่อตารางเซนติเมตร", "กิโลต่อตารางเซนติเมตร", "กกต่อตรซม", "kgf per cm2", "kg per cm2", "kgf per square centimeter", "kg per square centimeter"),
            lin("ทอร์", "Torr", 133.322368421, "ทอร์", "torr"),
        )
    ),
    // ฐาน: ลูกบาศก์เมตร/วินาที
    Category(
        "อัตราการไหล", listOf(
            lin("ลบ.ม./วินาที", "m³/s", 1.0, "ลูกบาศก์เมตรต่อวินาที", "ลบมต่อวินาที", "cubic meter per second", "m3 per s", "m3 per second"),
            lin("ลบ.ม./นาที", "m³/min", 1.0 / 60.0, "ลูกบาศก์เมตรต่อนาที", "ลบมต่อนาที", "cubic meter per minute", "m3 per min", "m3 per minute"),
            lin("ลบ.ม./ชั่วโมง", "m³/h", 1.0 / 3600.0, "ลูกบาศก์เมตรต่อชั่วโมง", "ลบมต่อชั่วโมง", "ลบมต่อชม",
                "cubic meter per hour", "m3 per h", "m3 per hour", "cmh"),
            lin("ลิตร/วินาที", "L/s", 0.001, "ลิตรต่อวินาที", "liter per second", "litre per second", "l per s", "lps"),
            lin("ลิตร/นาที", "L/min", 0.001 / 60.0, "ลิตรต่อนาที", "liter per minute", "litre per minute", "l per min", "lpm"),
            lin("ลิตร/ชั่วโมง", "L/h", 0.001 / 3600.0, "ลิตรต่อชั่วโมง", "liter per hour", "litre per hour", "l per h", "lph"),
            lin("ลบ.ฟุต/นาที", "CFM", 0.028316846592 / 60.0, "ซีเอฟเอ็ม", "ลูกบาศก์ฟุตต่อนาที", "cfm", "cubic foot per minute"),
            lin("ลบ.ฟุต/ชั่วโมง", "CFH", 0.028316846592 / 3600.0, "ซีเอฟเอช", "ลูกบาศก์ฟุตต่อชั่วโมง", "cfh", "cubic foot per hour"),
            lin("ลบ.ฟุต/วินาที", "ft³/s", 0.028316846592, "ลูกบาศก์ฟุตต่อวินาที", "cfs", "cubic foot per second"),
            lin("แกลลอน(US)/นาที", "GPM", 0.003785411784 / 60.0, "จีพีเอ็ม", "แกลลอนต่อนาที", "gpm", "gallon per minute"),
            lin("แกลลอน(US)/ชั่วโมง", "GPH", 0.003785411784 / 3600.0, "จีพีเอช", "แกลลอนต่อชั่วโมง", "gph", "gallon per hour"),
        )
    ),
    // ฐาน: เมตร/วินาที
    Category(
        "ความเร็ว", listOf(
            lin("เมตร/วินาที", "m/s", 1.0, "เมตรต่อวินาที", "meter per second", "metre per second", "m per s", "mps"),
            lin("เมตร/นาที", "m/min", 1.0 / 60.0, "เมตรต่อนาที", "meter per minute", "metre per minute", "m per min"),
            lin("กม./ชั่วโมง", "km/h", 1.0 / 3.6, "กิโลเมตรต่อชั่วโมง", "กิโลเมตรต่อชม", "กมต่อชม",
                "kilometer per hour", "kilometre per hour", "km per h", "kmh", "kph"),
            lin("ไมล์/ชั่วโมง", "mph", 0.44704, "ไมล์ต่อชั่วโมง", "mile per hour", "mph"),
            lin("ฟุต/วินาที", "ft/s", 0.3048, "ฟุตต่อวินาที", "foot per second", "ft per s", "fps"),
            lin("ฟุต/นาที", "FPM", 0.00508, "ฟุตต่อนาที", "foot per minute", "ft per min", "fpm"),
            lin("นอต", "kn", 1852.0 / 3600.0, "นอต", "knot", "kn"),
        )
    ),
    // ฐาน: กิโลกรัม
    Category(
        "น้ำหนัก", listOf(
            lin("มิลลิกรัม", "mg", 1e-6, "มิลลิกรัม", "milligram", "mg"),
            lin("กรัม", "g", 0.001, "กรัม", "gram"),
            lin("กิโลกรัม", "kg", 1.0, "กิโลกรัม", "กิโล", "kilogram", "kilo", "kg"),
            lin("เมตริกตัน", "t", 1000.0, "เมตริกตัน", "ตัน", "tonne", "metric ton"),
            lin("ออนซ์", "oz", 0.028349523125, "ออนซ์", "ounce", "oz"),
            lin("ปอนด์", "lb", 0.45359237, "ปอนด์", "pound", "lb", "lbs"),
            lin("ตันสั้น (US)", "short ton", 907.18474, "ตันสั้น", "short ton", "us ton"),
            lin("ตันยาว (UK)", "long ton", 1016.0469088, "ตันยาว", "long ton", "uk ton"),
        )
    ),
    // ฐาน: วินาที
    Category(
        "เวลา", listOf(
            lin("มิลลิวินาที", "ms", 0.001, "มิลลิวินาที", "millisecond", "ms"),
            lin("วินาที", "s", 1.0, "วินาที", "second", "sec"),
            lin("นาที", "min", 60.0, "นาที", "minute", "min"),
            lin("ชั่วโมง", "h", 3600.0, "ชั่วโมง", "ชม", "hour", "hr"),
            lin("วัน", "day", 86_400.0, "วัน", "day"),
            lin("สัปดาห์", "week", 604_800.0, "สัปดาห์", "อาทิตย์", "week"),
            lin("ปี (365.25 วัน)", "year", 31_557_600.0, "ปี", "year", "yr"),
        )
    ),
    // ฐาน: เมตร
    Category(
        "ระยะ", listOf(
            lin("มิลลิเมตร", "mm", 0.001, "มิลลิเมตร", "millimeter", "millimetre", "mm"),
            lin("เซนติเมตร", "cm", 0.01, "เซนติเมตร", "centimeter", "centimetre", "cm"),
            lin("เมตร", "m", 1.0, "เมตร", "meter", "metre", "m"),
            lin("กิโลเมตร", "km", 1000.0, "กิโลเมตร", "kilometer", "kilometre", "km"),
            lin("นิ้ว", "in", 0.0254, "นิ้ว", "inch"),
            lin("ฟุต", "ft", 0.3048, "ฟุต", "foot", "ft"),
            lin("หลา", "yd", 0.9144, "หลา", "yard", "yd"),
            lin("ไมล์", "mi", 1609.344, "ไมล์", "mile", "mi"),
            lin("ไมล์ทะเล", "nmi", 1852.0, "ไมล์ทะเล", "nautical mile", "nmi"),
        )
    ),
    // ฐาน: ตารางเมตร
    Category(
        "พื้นที่", listOf(
            lin("ตร.มิลลิเมตร", "mm²", 1e-6, "ตารางมิลลิเมตร", "square millimeter", "mm2"),
            lin("ตร.เซนติเมตร", "cm²", 1e-4, "ตารางเซนติเมตร", "ตรซม", "square centimeter", "cm2"),
            lin("ตร.เมตร", "m²", 1.0, "ตารางเมตร", "ตรม", "square meter", "square metre", "m2", "sqm"),
            lin("ตร.กิโลเมตร", "km²", 1e6, "ตารางกิโลเมตร", "ตรกม", "square kilometer", "km2"),
            lin("เฮกตาร์", "ha", 10_000.0, "เฮกตาร์", "hectare", "ha"),
            lin("ตร.วา", "ตร.ว.", 4.0, "ตารางวา", "ตรว"),
            lin("งาน", "งาน", 400.0, "งาน"),
            lin("ไร่", "ไร่", 1600.0, "ไร่"),
            lin("ตร.นิ้ว", "in²", 0.00064516, "ตารางนิ้ว", "square inch", "in2", "sq in"),
            lin("ตร.ฟุต", "ft²", 0.09290304, "ตารางฟุต", "square foot", "ft2", "sq ft"),
            lin("ตร.หลา", "yd²", 0.83612736, "ตารางหลา", "square yard", "yd2"),
            lin("เอเคอร์", "acre", 4046.8564224, "เอเคอร์", "acre"),
        )
    ),
    // ฐาน: ลูกบาศก์เมตร
    Category(
        "ปริมาตร", listOf(
            lin("มิลลิลิตร", "mL", 1e-6, "มิลลิลิตร", "ซีซี", "milliliter", "millilitre", "ml", "cc"),
            lin("ลิตร", "L", 0.001, "ลิตร", "liter", "litre", "l"),
            lin("ลูกบาศก์เมตร", "m³", 1.0, "ลูกบาศก์เมตร", "ลบม", "cubic meter", "cubic metre", "m3", "cbm"),
            lin("ลบ.นิ้ว", "in³", 1.6387064e-5, "ลูกบาศก์นิ้ว", "cubic inch", "in3"),
            lin("ลบ.ฟุต", "ft³", 0.028316846592, "ลูกบาศก์ฟุต", "cubic foot", "ft3", "cuft"),
            lin("แกลลอน (US)", "gal US", 0.003785411784, "แกลลอน", "gallon", "gal", "us gallon"),
            lin("แกลลอน (UK)", "gal UK", 0.00454609, "แกลลอนอังกฤษ", "uk gallon", "imperial gallon"),
            lin("บาร์เรล (น้ำมัน)", "bbl", 0.158987294928, "บาร์เรล", "barrel", "bbl"),
        )
    ),
    // ฐาน: จูล
    Category(
        "พลังงาน", listOf(
            lin("จูล", "J", 1.0, "จูล", "joule"),
            lin("กิโลจูล", "kJ", 1000.0, "กิโลจูล", "kilojoule", "kj"),
            lin("เมกะจูล", "MJ", 1_000_000.0, "เมกะจูล", "megajoule", "mj"),
            lin("วัตต์-ชั่วโมง", "Wh", 3600.0, "วัตต์ชั่วโมง", "watt hour", "wh"),
            lin("กิโลวัตต์-ชั่วโมง", "kWh", 3_600_000.0, "กิโลวัตต์ชั่วโมง", "ยูนิต", "kilowatt hour", "kwh"),
            lin("แคลอรี", "cal", 4.184, "แคลอรี่", "แคลอรี", "calorie", "cal"),
            lin("กิโลแคลอรี", "kcal", 4184.0, "กิโลแคลอรี่", "กิโลแคลอรี", "kilocalorie", "kcal"),
            lin("บีทียู (IT)", "BTU", 1055.05585262, "บีทียู", "british thermal unit", "btu"),
            lin("ตันความเย็น-ชั่วโมง", "TR·h", 12_000.0 * 1055.05585262, "ตันความเย็นชั่วโมง", "ton refrigeration hour", "tr hour"),
            lin("ฟุต-ปอนด์แรง", "ft·lbf", 1.3558179483314004, "ฟุตปอนด์", "foot pound", "ft lbf", "ftlbf"),
        )
    ),
)

/** จัดรูปตัวเลข: 7 หลักนัยสำคัญ, ใช้รูปแบบวิทยาศาสตร์เมื่อใหญ่/เล็กมาก */
fun formatNumber(v: Double): String {
    if (v.isNaN() || v.isInfinite()) return "—"
    if (v == 0.0) return "0"
    val a = abs(v)
    return if (a >= 1e-4 && a < 1e9) {
        BigDecimal(v).round(MathContext(7)).stripTrailingZeros().toPlainString()
    } else {
        String.format(Locale.US, "%.5e", v)
    }
}
