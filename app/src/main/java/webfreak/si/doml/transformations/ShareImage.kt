package webfreak.si.doml.transformations

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.security.MessageDigest
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint

class ShareImage(ctx: Context) : BitmapTransformation() {

    val context: Context = ctx

    override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        return drawMultilineTextToBitmap(context, toTransform, "This is a beautiful day indeed!")
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {}

    fun drawMultilineTextToBitmap(gContext: Context, gResId: Bitmap, gText: String): Bitmap {
        var gText = gText
        if (gText.contains("You have just")) {
            gText = gText.replace("You have just", "I have just")
        }
        // prepare canvas
        val resources = gContext.resources
        val scale = resources.displayMetrics.density
        var bitmap = gResId

        var bitmapConfig: android.graphics.Bitmap.Config? = bitmap.config
        // set default bitmap config if none
        if (bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888
        }
        // resource bitmaps are imutable,
        // so we need to convert it to mutable one
        bitmap = bitmap.copy(bitmapConfig, true)

        val canvas = Canvas(bitmap)
        val canvasSignature = Canvas(bitmap)
        val canvasSignature2 = Canvas(bitmap)
        // new antialiased Paint
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        val signature = TextPaint(Paint.ANTI_ALIAS_FLAG)// text color - #3D3D3D
        val signature2 = TextPaint(Paint.ANTI_ALIAS_FLAG)// text color - #3D3D3D
        paint.color = Color.WHITE
        signature.color = Color.WHITE
        signature2.color = Color.WHITE
        // text size in pixels

        paint.textSize = (55 * scale).toInt().toFloat()
        signature.textSize = (35 * scale).toInt().toFloat()
        signature2.textSize = (33 * scale).toInt().toFloat()
        // text shadow
        paint.setShadowLayer(3f, 8f, 8f, Color.BLACK)
        signature.setShadowLayer(3f, 8f, 8f, Color.BLACK)
        signature2.setShadowLayer(3f, 8f, 8f, Color.BLACK)
        // set text width to canvas width minus 16dp padding
        val textWidth = canvas.width - (16 * scale).toInt()

        // init StaticLayout for text
        val textLayout = StaticLayout(gText, paint, textWidth, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false)
        val signatureLayout = StaticLayout("@DaysOfMyLife", signature, textWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false)
        val signatureLayout2 = StaticLayout("goo.gl/PTBn6D", signature, textWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false)

        // get height of multiline text
        val textHeight = textLayout.height

        // get position of text's top left corner
        val x = ((bitmap.width - textWidth) / 2).toFloat()
        val y = ((bitmap.height - textHeight) / 2).toFloat()

        // draw text to the Canvas center
        canvas.save()
        canvasSignature.save()
        canvasSignature2.save()
        canvas.translate(x, y)
        canvasSignature.translate(20f, 1600f)
        val width = bitmap.width
        canvasSignature2.translate(width - 650f, 1600f)
        textLayout.draw(canvas)
        signatureLayout.draw(canvasSignature)
        signatureLayout2.draw(canvasSignature2)
        canvas.restore()
        canvasSignature.restore()
        canvasSignature2.restore()

        return bitmap
    }
}