package com.shenzhiyao.days.image

import android.content.Context
import android.graphics.*
import android.net.Uri
import com.shenzhiyao.days.data.WidgetAspect
import java.io.File
import java.io.IOException
import kotlin.math.max

object BackgroundImageStore {
    private const val MAX_DECODE = 2048
    private const val WIDE_W = 900
    private const val WIDE_H = 450
    private const val SQUARE = 720

    /** Copies a safely sampled, center-cropped JPEG into private storage; the picker URI is never retained. */
    fun import(context: Context, uri: Uri, aspect: WidgetAspect, zoom: Float, offsetX: Float, offsetY: Float): String {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            ?: throw IOException("无法读取图片")
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) throw IOException("图片格式无法解析")
        var sample = 1
        while (max(bounds.outWidth, bounds.outHeight) / sample > MAX_DECODE) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample; inPreferredConfig = Bitmap.Config.ARGB_8888 }
        val source = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
            ?: throw IOException("图片解码失败")
        val (outW, outH) = if (aspect == WidgetAspect.WIDE) WIDE_W to WIDE_H else SQUARE to SQUARE
        val result = Bitmap.createBitmap(outW, outH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val baseScale = max(outW.toFloat() / source.width, outH.toFloat() / source.height)
        val scale = baseScale * zoom.coerceIn(1f, 4f)
        val drawnW = source.width * scale
        val drawnH = source.height * scale
        val maxX = ((drawnW - outW) / 2f).coerceAtLeast(0f)
        val maxY = ((drawnH - outH) / 2f).coerceAtLeast(0f)
        val left = (outW - drawnW) / 2f + offsetX.coerceIn(-1f, 1f) * maxX
        val top = (outH - drawnH) / 2f + offsetY.coerceIn(-1f, 1f) * maxY
        canvas.drawBitmap(source, null, RectF(left, top, left + drawnW, top + drawnH), Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
        val dir = File(context.filesDir, "widget_backgrounds").apply { mkdirs() }
        val output = File(dir, "bg_${System.currentTimeMillis()}_${(0..9999).random()}.jpg")
        try {
            output.outputStream().buffered().use { if (!result.compress(Bitmap.CompressFormat.JPEG, 86, it)) throw IOException("图片压缩失败") }
        } finally { source.recycle(); result.recycle() }
        return output.absolutePath
    }

    fun deleteIfUnused(path: String?, usedPaths: Collection<String>) {
        if (path != null && path !in usedPaths) runCatching { File(path).takeIf { it.isFile }?.delete() }
    }
}
