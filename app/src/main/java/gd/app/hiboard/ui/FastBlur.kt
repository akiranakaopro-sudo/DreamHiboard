package gd.app.hiboard.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect

/**
 * Stack Blur (Mario Klingemann), same path ColorOS InCall uses via Dialer's FastBlur.
 * Downscale first, then blur — cheap baked frost instead of a live compositor blur.
 */
object FastBlur {

    fun doBlur(sentBitmap: Bitmap, radius: Int, canReuseInBitmap: Boolean): Bitmap? {
        if (radius < 1) {
            return null
        }
        val bitmap = if (canReuseInBitmap) {
            sentBitmap
        } else {
            sentBitmap.copy(sentBitmap.config ?: Bitmap.Config.ARGB_8888, true) ?: return null
        }

        val w = bitmap.width
        val h = bitmap.height
        val pix = IntArray(w * h)
        bitmap.getPixels(pix, 0, w, 0, 0, w, h)

        val wm = w - 1
        val hm = h - 1
        val wh = w * h
        val div = radius + radius + 1

        val r = IntArray(wh)
        val g = IntArray(wh)
        val b = IntArray(wh)
        val vmin = IntArray(maxOf(w, h))

        var divsum = (div + 1) shr 1
        divsum *= divsum
        val dv = IntArray(256 * divsum)
        for (i in dv.indices) {
            dv[i] = i / divsum
        }

        var yw = 0
        var yi = 0
        val stack = Array(div) { IntArray(3) }
        val r1 = radius + 1

        for (y in 0 until h) {
            var rinsum = 0
            var ginsum = 0
            var binsum = 0
            var routsum = 0
            var goutsum = 0
            var boutsum = 0
            var rsum = 0
            var gsum = 0
            var bsum = 0
            for (i in -radius..radius) {
                val p = pix[yi + i.coerceIn(0, wm)]
                val sir = stack[i + radius]
                sir[0] = p shr 16 and 0xff
                sir[1] = p shr 8 and 0xff
                sir[2] = p and 0xff
                val rbs = r1 - abs(i)
                rsum += sir[0] * rbs
                gsum += sir[1] * rbs
                bsum += sir[2] * rbs
                if (i > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }
            }
            var stackpointer = radius
            for (x in 0 until w) {
                r[yi] = dv[rsum]
                g[yi] = dv[gsum]
                b[yi] = dv[bsum]

                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum

                val stackstart = stackpointer - radius + div
                val sir = stack[stackstart % div]
                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]

                if (y == 0) {
                    vmin[x] = minOf(x + radius + 1, wm)
                }
                val p = pix[yw + vmin[x]]
                sir[0] = p shr 16 and 0xff
                sir[1] = p shr 8 and 0xff
                sir[2] = p and 0xff

                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]

                rsum += rinsum
                gsum += ginsum
                bsum += binsum

                stackpointer = (stackpointer + 1) % div
                val sirNext = stack[stackpointer % div]
                routsum += sirNext[0]
                goutsum += sirNext[1]
                boutsum += sirNext[2]
                rinsum -= sirNext[0]
                ginsum -= sirNext[1]
                binsum -= sirNext[2]
                yi++
            }
            yw += w
        }

        for (x in 0 until w) {
            var rinsum = 0
            var ginsum = 0
            var binsum = 0
            var routsum = 0
            var goutsum = 0
            var boutsum = 0
            var rsum = 0
            var gsum = 0
            var bsum = 0
            var yp = -radius * w
            for (i in -radius..radius) {
                yi = maxOf(0, yp) + x
                val sir = stack[i + radius]
                sir[0] = r[yi]
                sir[1] = g[yi]
                sir[2] = b[yi]
                val rbs = r1 - abs(i)
                rsum += r[yi] * rbs
                gsum += g[yi] * rbs
                bsum += b[yi] * rbs
                if (i > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }
                if (i < hm) {
                    yp += w
                }
            }
            yi = x
            var stackpointer = radius
            for (y in 0 until h) {
                pix[yi] = (0xff000000.toInt() and pix[yi]) or
                    (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum]

                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum

                val stackstart = stackpointer - radius + div
                val sir = stack[stackstart % div]
                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]

                if (x == 0) {
                    vmin[y] = minOf(y + r1, hm) * w
                }
                val p = x + vmin[y]
                sir[0] = r[p]
                sir[1] = g[p]
                sir[2] = b[p]

                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]

                rsum += rinsum
                gsum += ginsum
                bsum += binsum

                stackpointer = (stackpointer + 1) % div
                val sirNext = stack[stackpointer]
                routsum += sirNext[0]
                goutsum += sirNext[1]
                boutsum += sirNext[2]
                rinsum -= sirNext[0]
                ginsum -= sirNext[1]
                binsum -= sirNext[2]
                yi += w
            }
        }

        bitmap.setPixels(pix, 0, w, 0, 0, w, h)
        return bitmap
    }

    /**
     * @param scale downsample factor before blur (Dialer InCall uses 4)
     * @param useMixColor if true, [color] is drawn over the result (Dialer button blur)
     */
    fun doBlur(
        sentBitmap: Bitmap,
        radius: Int,
        scale: Int,
        useMixColor: Boolean,
        color: Int,
    ): Bitmap? {
        val s = scale.coerceIn(1, 20)
        val width = sentBitmap.width
        val height = sentBitmap.height
        val scaleBmp = if (sentBitmap.config == Bitmap.Config.ARGB_8888) {
            Bitmap.createScaledBitmap(sentBitmap, (width / s).coerceAtLeast(1), (height / s).coerceAtLeast(1), false)
        } else {
            Bitmap.createBitmap((width / s).coerceAtLeast(1), (height / s).coerceAtLeast(1), Bitmap.Config.ARGB_8888).also { out ->
                val canvas = Canvas(out)
                canvas.drawBitmap(
                    sentBitmap,
                    Rect(0, 0, sentBitmap.width, sentBitmap.height),
                    Rect(0, 0, out.width, out.height),
                    null,
                )
            }
        }
        val blurBmp = doBlur(scaleBmp, radius, false) ?: return null
        if (useMixColor) {
            Canvas(blurBmp).drawColor(color)
        }
        return blurBmp
    }

    private fun abs(v: Int): Int = if (v < 0) -v else v
}
