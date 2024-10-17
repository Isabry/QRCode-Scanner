package fr.sabry.qrcodescanner

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Vibrator
import androidx.core.content.ContextCompat.getSystemService
import com.google.mlkit.vision.barcode.common.Barcode

fun getBarcodeFormat(format: Int) : String{
    when (format) {
        Barcode.FORMAT_UNKNOWN -> return "UNKNOWN"
        Barcode.FORMAT_ALL_FORMATS -> return ""
        Barcode.FORMAT_CODE_128 -> return "FORMAT_CODE_128"
        Barcode.FORMAT_CODE_39 -> return "FORMAT_CODE_39"
        Barcode.FORMAT_CODE_93 -> return "FORMAT_CODE_93"
        Barcode.FORMAT_CODABAR -> return "FORMAT_CODABAR"
        Barcode.FORMAT_DATA_MATRIX -> return "DATA_MATRIX"
        Barcode.FORMAT_EAN_13 -> return "EAN_13"
        Barcode.FORMAT_EAN_8 -> return "EAN_8"
        Barcode.FORMAT_ITF -> return "ITF"
        Barcode.FORMAT_QR_CODE -> return "QR_CODE"
        Barcode.FORMAT_UPC_A -> return "UPC_A"
        Barcode.FORMAT_UPC_E -> return "UPC_E"
        Barcode.FORMAT_PDF417 -> return "PDF417"
        Barcode.FORMAT_AZTEC -> return "AZTEC"
        else -> return "Unknown"
    }
}

fun getBarcodeType(type: Int): String {
    when (type) {
        Barcode.TYPE_UNKNOWN -> return "UNKNOWN"
        Barcode.TYPE_CONTACT_INFO -> return "CONTACT_INFO"
        Barcode.TYPE_EMAIL -> return "EMAIL"
        Barcode.TYPE_ISBN -> return "ISBN"
        Barcode.TYPE_PHONE -> return "PHONE"
        Barcode.TYPE_PRODUCT -> return "PRODUCT"
        Barcode.TYPE_SMS -> return "SMS"
        Barcode.TYPE_TEXT -> return "TEXT"
        Barcode.TYPE_URL -> return "URL"
        Barcode.TYPE_WIFI -> return "WIFI"
        Barcode.TYPE_GEO -> return "GEO"
        Barcode.TYPE_CALENDAR_EVENT -> return "CALENDAR_EVENT"
        Barcode.TYPE_DRIVER_LICENSE -> return "DRIVER_LICENSE"
        else -> return "Unknown"
    }
}

fun beep() {
    val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
    toneGen.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200)
}

fun vibrate(context: Context) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    vibrator.vibrate(500)
}