package com.engunits.converter

/** ภาษาของหน้าจอแอป (และภาษาที่ใช้รู้จำเสียง) */
enum class AppLang(val tag: String) { TH("th-TH"), EN("en-US") }

private val CategoryTitlesEn = mapOf(
    "อุณหภูมิ" to "Temperature",
    "ความดัน" to "Pressure",
    "อัตราการไหล" to "Flow rate",
    "ความเร็ว" to "Speed",
    "น้ำหนัก" to "Weight",
    "เวลา" to "Time",
    "ระยะ" to "Length",
    "พื้นที่" to "Area",
    "ปริมาตร" to "Volume",
    "พลังงาน" to "Energy",
)

/** ชื่อหน่วยภาษาอังกฤษ อ้างด้วยสัญลักษณ์ (symbol) ซึ่งไม่ซ้ำกันในทุกหมวด */
private val UnitNamesEn = mapOf(
    "°C" to "Degree Celsius", "°F" to "Degree Fahrenheit", "K" to "Kelvin", "°R" to "Degree Rankine",
    "Pa" to "Pascal", "kPa" to "Kilopascal", "MPa" to "Megapascal", "bar" to "Bar", "mbar" to "Millibar",
    "atm" to "Atmosphere", "psi" to "Pound per square inch", "inH₂O" to "Inch of water (4°C)",
    "mmH₂O" to "Millimeter of water", "mmHg" to "Millimeter of mercury", "inHg" to "Inch of mercury",
    "kgf/cm²" to "Kilogram-force per cm²", "Torr" to "Torr",
    "m³/s" to "Cubic meter per second", "m³/min" to "Cubic meter per minute", "m³/h" to "Cubic meter per hour",
    "L/s" to "Liter per second", "L/min" to "Liter per minute", "L/h" to "Liter per hour",
    "CFM" to "Cubic foot per minute", "CFH" to "Cubic foot per hour", "ft³/s" to "Cubic foot per second",
    "GPM" to "Gallon (US) per minute", "GPH" to "Gallon (US) per hour",
    "m/s" to "Meter per second", "m/min" to "Meter per minute", "km/h" to "Kilometer per hour",
    "mph" to "Mile per hour", "ft/s" to "Foot per second", "FPM" to "Foot per minute", "kn" to "Knot",
    "mg" to "Milligram", "g" to "Gram", "kg" to "Kilogram", "t" to "Metric ton", "oz" to "Ounce",
    "lb" to "Pound", "short ton" to "Short ton (US)", "long ton" to "Long ton (UK)",
    "ms" to "Millisecond", "s" to "Second", "min" to "Minute", "h" to "Hour", "day" to "Day",
    "week" to "Week", "year" to "Year (365.25 days)",
    "mm" to "Millimeter", "cm" to "Centimeter", "m" to "Meter", "km" to "Kilometer", "in" to "Inch",
    "ft" to "Foot", "yd" to "Yard", "mi" to "Mile", "nmi" to "Nautical mile",
    "mm²" to "Square millimeter", "cm²" to "Square centimeter", "m²" to "Square meter",
    "km²" to "Square kilometer", "ha" to "Hectare", "ตร.ว." to "Square wah (Thai, 4 m²)",
    "งาน" to "Ngan (Thai, 400 m²)", "ไร่" to "Rai (Thai, 1,600 m²)", "in²" to "Square inch",
    "ft²" to "Square foot", "yd²" to "Square yard", "acre" to "Acre",
    "mL" to "Milliliter", "L" to "Liter", "m³" to "Cubic meter", "in³" to "Cubic inch", "ft³" to "Cubic foot",
    "gal US" to "Gallon (US)", "gal UK" to "Gallon (UK)", "bbl" to "Barrel (oil)",
    "J" to "Joule", "kJ" to "Kilojoule", "MJ" to "Megajoule", "Wh" to "Watt-hour", "kWh" to "Kilowatt-hour",
    "cal" to "Calorie", "kcal" to "Kilocalorie", "BTU" to "BTU (IT)", "TR·h" to "Ton of refrigeration-hour",
    "ft·lbf" to "Foot-pound force",
)

/** ข้อความทั้งหมดบนหน้าจอ สองภาษา */
class Ui(val lang: AppLang) {
    private val th get() = lang == AppLang.TH
    private fun t(thai: String, eng: String) = if (th) thai else eng

    val appTitle = "Unit Converter"
    val valueLabel get() = t("ค่าที่ต้องการแปลง", "Value to convert")
    val hint get() = t(
        "แตะไมค์เพื่อสั่งด้วยเสียง · แตะแถวเพื่อใช้เป็นหน่วยต้นทาง · กดค้างเพื่อคัดลอก",
        "Tap the mic to speak · Tap a row to use it as the source unit · Long-press to copy",
    )
    val micDesc get() = t("สั่งงานด้วยเสียง", "Voice command")
    val languageDesc get() = t("เปลี่ยนภาษา", "Change language")
    val flagThDesc = "ไทย"
    val flagEnDesc = "English"
    val noRecognizer get() = t(
        "เครื่องนี้ไม่มีบริการรู้จำเสียง (ต้องมีแอป Google)",
        "Speech recognition isn't available on this device (the Google app is required)",
    )
    val voicePrompt get() = t(
        "พูดคำสั่ง เช่น แปลง 100 CFM เป็น ลูกบาศก์เมตรต่อชั่วโมง",
        "Say e.g. convert 100 CFM to cubic meter per hour",
    )
    val heardNothing get() = t("ไม่ได้ยินเสียง ลองใหม่อีกครั้ง", "Didn't hear anything. Try again.")
    fun heard(h: String) = t("ได้ยิน: \"$h\"", "Heard: \"$h\"")
    fun notUnderstood(h: String) = t(
        "ไม่เข้าใจคำสั่ง \"$h\" ลองพูดเช่น \"แปลง 100 CFM เป็น ลูกบาศก์เมตรต่อชั่วโมง\"",
        "Couldn't understand \"$h\". Try: \"convert 100 CFM to cubic meter per hour\"",
    )
    fun copied(v: String) = t("คัดลอกแล้ว: $v", "Copied: $v")

    fun catTitle(c: Category): String = if (th) c.title else CategoryTitlesEn[c.title] ?: c.title
    fun unitName(u: UnitDef): String = if (th) u.name else UnitNamesEn[u.symbol] ?: u.name
    fun unitMenuLabel(u: UnitDef) = "${unitName(u)} (${u.symbol})"
}
