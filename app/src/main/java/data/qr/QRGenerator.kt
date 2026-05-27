package data.qr

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

object QRGenerator {
    
    /**
     * Genera un código QR a partir de un UUID de negocio
     * @param businessId UUID del negocio
     * @param width Ancho del QR en píxeles (default: 500)
     * @param height Alto del QR en píxeles (default: 500)
     * @return Bitmap con el código QR o null si hay error
     */
    fun generateQRCode(businessId: String, width: Int = 500, height: Int = 500): Bitmap? {
        return try {
            val qrContent = "aia://business/$businessId"
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(qrContent, BarcodeFormat.QR_CODE, width, height)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
