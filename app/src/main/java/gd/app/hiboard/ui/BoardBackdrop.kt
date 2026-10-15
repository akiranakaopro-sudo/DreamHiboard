package gd.app.hiboard.ui

import android.app.Activity
import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import gd.app.hiboard.HiboardActivity
import gd.app.hiboard.R
import java.lang.reflect.Method

/**
 * Same frost as DreamQuickSearch [gd.app.quicksearch.ui.home.SearchHomeBackdrop]:
 * bake the system wallpaper with [FastBlur] when pixels can be read, otherwise
 * frost the live wallpaper with blur-behind and an Oplus Kawase drawable.
 *
 * Window flags are applied only for [HiboardActivity]. A launcher-embedded
 * board still paints the baked bitmap, and tries the blur drawable without
 * changing the host window.
 */
class BoardBackdrop(
    private val context: Context,
    private val backdropView: ImageView,
    private val blurHost: View,
) {
    private val wallpaperManager = WallpaperManager.getInstance(context)
    private val activity: Activity? = context.findActivity()
    private var colorsListener: WallpaperManager.OnColorsChangedListener? = null
    private var liveBlurDrawable: Drawable? = null
    @Volatile
    private var released = false
    private var usingLiveBlur = false

    fun apply() {
        if (activity is HiboardActivity) {
            applyCachedToWindow(activity)
        }
        colorsListener = listenForWallpaperChange()
        paintBackdrop()
    }

    fun release() {
        released = true
        colorsListener?.let { listener ->
            runCatching { wallpaperManager.removeOnColorsChangedListener(listener) }
        }
        colorsListener = null
        liveBlurDrawable = null
        blurHost.background = null
    }

    private fun listenForWallpaperChange(): WallpaperManager.OnColorsChangedListener? {
        return runCatching {
            val listener = WallpaperManager.OnColorsChangedListener { _, which ->
                if (!released && which and WallpaperManager.FLAG_SYSTEM != 0) {
                    invalidateAndReload()
                }
            }
            wallpaperManager.addOnColorsChangedListener(listener, Handler(Looper.getMainLooper()))
            listener
        }.getOrNull()
    }

    private fun paintBackdrop() {
        ensureBlurredWallpaper(context.applicationContext)
        val blur = copyCachedBlur(context)
        if (blur != null) {
            usingLiveBlur = false
            clearLiveWallpaperFlags()
            backdropView.setImageDrawable(blur)
            backdropView.visibility = View.VISIBLE
            blurHost.background = null
            blurHost.visibility = View.GONE
            if (activity is HiboardActivity) {
                activity.window.setBackgroundDrawable(copyCachedBlur(context))
            }
            return
        }

        usingLiveBlur = true
        enableLiveWallpaperFlags()
        backdropView.setImageDrawable(null)
        backdropView.visibility = View.GONE
        blurHost.background = null
        blurHost.visibility = View.VISIBLE
        blurHost.post {
            if (!released && usingLiveBlur) {
                val ok = frostWallpaperInCompositor(blurHost)
                Log.i(TAG, "live frost drawable applied=$ok")
            }
        }
    }

    private fun enableLiveWallpaperFlags() {
        val window = (activity as? HiboardActivity)?.window ?: return
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER or
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
        )
        window.setBackgroundDrawableResource(android.R.color.transparent)
        if (Build.VERSION.SDK_INT >= 31) {
            window.attributes = window.attributes.apply {
                blurBehindRadius = LIVE_BLUR_RADIUS_PX
            }
            window.setBackgroundBlurRadius(LIVE_BLUR_RADIUS_PX)
        }
    }

    private fun clearLiveWallpaperFlags() {
        val window = (activity as? HiboardActivity)?.window ?: return
        window.clearFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER or
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
        )
        if (Build.VERSION.SDK_INT >= 31) {
            window.setBackgroundBlurRadius(0)
        }
    }

    private fun frostWallpaperInCompositor(target: View): Boolean {
        return runCatching {
            val radius = target.resources.getDimensionPixelSize(R.dimen.hiboard_live_blur_radius)
            val drawable = createBlurDrawable(target) ?: return false
            val params = newBlurParam() ?: return false
            call(drawable, "setBlurRadius", arrayOf(Int::class.javaPrimitiveType!!), radius)
            call(drawable, "setColor", arrayOf(Int::class.javaPrimitiveType!!), Color.TRANSPARENT)
            val wrapper = call(drawable, "getWrapper")
            val ext = call(wrapper, "getExtImpl")
            val paramClass = Class.forName("com.oplus.graphics.OplusBlurParam")
            call(ext, "setBlurParams", arrayOf(paramClass), params)
            target.background = drawable
            liveBlurDrawable = drawable
            true
        }.getOrDefault(false)
    }

    private fun newBlurParam(): Any? {
        return runCatching {
            val type = Class.forName("com.oplus.graphics.OplusBlurParam")
            val params = type.getDeclaredConstructor().newInstance()
            val kawase = type.getField("BLUR_TYPE_QUALITY_KAWASE").getInt(null)
            val overlay = type.getField("BLUR_BLEND_MODE_OVERLAY").getInt(null)
            type.getMethod("setBlurType", Int::class.javaPrimitiveType).invoke(params, kawase)
            type.getMethod(
                "setMaterialParams",
                Int::class.javaPrimitiveType,
                FloatArray::class.java,
                FloatArray::class.java,
            ).invoke(
                params,
                overlay,
                floatArrayOf(0f, 0f, 0f, 0.06f),
                floatArrayOf(0f, 0f, 0f, 0.16f),
            )
            params
        }.getOrNull()
    }

    private fun createBlurDrawable(target: View): Drawable? {
        val fromManager = runCatching {
            val manager = Class.forName("com.oplus.view.ViewRootManager")
                .getConstructor(View::class.java)
                .newInstance(target)
            manager.javaClass.getMethod("getBackgroundBlurDrawable").invoke(manager) as? Drawable
        }.getOrNull()
        if (fromManager != null) {
            return fromManager
        }
        val viewRootImpl = call(target, "getViewRootImpl") ?: return null
        return call(viewRootImpl, "createBackgroundBlurDrawable") as? Drawable
    }

    private fun call(
        target: Any?,
        name: String,
        types: Array<Class<*>> = emptyArray(),
        vararg args: Any?,
    ): Any? {
        if (target == null) {
            return null
        }
        var type: Class<*>? = target.javaClass
        while (type != null) {
            try {
                val method: Method = type.getDeclaredMethod(name, *types)
                method.isAccessible = true
                return method.invoke(target, *args)
            } catch (_: NoSuchMethodException) {
                type = type.superclass
            } catch (_: Exception) {
                return null
            }
        }
        return null
    }

    private fun invalidateAndReload() {
        synchronized(BLUR_LOCK) {
            sCachedBlurredWallpaper = null
            clearWallpaperSourceCache(context.applicationContext)
        }
        Thread({
            ensureBlurredWallpaper(context.applicationContext)
            if (!released) {
                val host = activity
                if (host != null) {
                    host.runOnUiThread {
                        if (!released) paintBackdrop()
                    }
                } else {
                    Handler(Looper.getMainLooper()).post {
                        if (!released) paintBackdrop()
                    }
                }
            }
        }, "hiboard-blur-reload").apply {
            priority = Thread.NORM_PRIORITY - 1
            start()
        }
    }

    companion object {
        private const val TAG = "BoardBackdrop"
        private const val BLUR_RADIUS = 25
        private const val BLUR_SCALE = 4
        private const val MAX_WALLPAPER_EDGE_PX = 720
        private const val LIVE_BLUR_RADIUS_PX = 150
        private const val WALLPAPER_CACHE_NAME = "wallpaper.bin"

        private val BLUR_LOCK = Any()
        @Volatile
        private var sCachedBlurredWallpaper: Drawable? = null

        fun prefetch(context: Context?) {
            if (context == null || sCachedBlurredWallpaper != null) {
                return
            }
            Thread(
                { ensureBlurredWallpaper(context.applicationContext) },
                "hiboard-blur-prefetch",
            ).apply {
                priority = Thread.NORM_PRIORITY - 1
                start()
            }
        }

        fun applyCachedToWindow(activity: Activity) {
            ensureBlurredWallpaper(activity.applicationContext)
            val blur = copyCachedBlur(activity)
            if (blur != null) {
                activity.window.setBackgroundDrawable(blur)
            } else {
                activity.window.setBackgroundDrawableResource(android.R.color.transparent)
            }
        }

        private fun copyCachedBlur(context: Context): Drawable? {
            val cached = sCachedBlurredWallpaper
            if (cached is BitmapDrawable) {
                val bmp = cached.bitmap
                if (bmp != null && !bmp.isRecycled) {
                    return BitmapDrawable(context.resources, bmp)
                }
            }
            return cached
        }

        private fun ensureBlurredWallpaper(context: Context) {
            if (sCachedBlurredWallpaper != null) {
                return
            }
            synchronized(BLUR_LOCK) {
                if (sCachedBlurredWallpaper != null) {
                    return
                }
                val wallpaper = loadBlurredWallpaperBackground(context)
                if (wallpaper != null) {
                    sCachedBlurredWallpaper = wallpaper
                }
            }
        }

        private fun loadBlurredWallpaperBackground(context: Context): Drawable? {
            return runCatching {
                val wm = WallpaperManager.getInstance(context) ?: return null
                val wallpaperBitmap = loadWallpaperBitmap(wm, context) ?: return null
                if (wallpaperBitmap.width <= 0) {
                    return null
                }
                Log.i(TAG, "baking frost ${wallpaperBitmap.width}x${wallpaperBitmap.height}")
                val toBlur = if (wallpaperBitmap.config != Bitmap.Config.ARGB_8888) {
                    wallpaperBitmap.copy(Bitmap.Config.ARGB_8888, false) ?: wallpaperBitmap
                } else {
                    wallpaperBitmap
                }
                val blurred = FastBlur.doBlur(toBlur, BLUR_RADIUS, BLUR_SCALE, false, 0)
                    ?: return BitmapDrawable(context.resources, wallpaperBitmap)
                Log.i(TAG, "frost ready ${blurred.width}x${blurred.height}")
                cacheWallpaperSource(context, toBlur)
                BitmapDrawable(context.resources, blurred)
            }.onFailure {
                Log.w(TAG, "loadBlurredWallpaperBackground failed", it)
            }.getOrNull()
        }

        private fun cacheWallpaperSource(context: Context, source: Bitmap) {
            runCatching {
                val out = java.io.File(context.filesDir, WALLPAPER_CACHE_NAME)
                java.io.FileOutputStream(out).use { fos ->
                    source.compress(Bitmap.CompressFormat.JPEG, 85, fos)
                }
            }
        }

        private fun clearWallpaperSourceCache(context: Context) {
            runCatching {
                java.io.File(context.filesDir, WALLPAPER_CACHE_NAME).delete()
            }
        }

        private fun loadWallpaperBitmap(wm: WallpaperManager, context: Context): Bitmap? {
            decodeWallpaperFile(wm)?.let { return it }
            readWallpaperBitmap(wm)?.let { return it }
            val drawable = runCatching { wm.peekFastDrawable() }.getOrNull()
                ?: runCatching { wm.peekDrawable() }.getOrNull()
                ?: runCatching { wm.fastDrawable }.getOrNull()
                ?: runCatching { wm.drawable }.getOrNull()
            if (drawable is BitmapDrawable && drawable.bitmap != null && drawable.bitmap.hasVisibleColor()) {
                return drawable.bitmap
            }
            if (drawable != null) {
                val width = drawable.intrinsicWidth.takeIf { it > 0 }
                    ?: context.resources.displayMetrics.widthPixels
                val height = drawable.intrinsicHeight.takeIf { it > 0 }
                    ?: context.resources.displayMetrics.heightPixels
                val sample = sampleSize(width, height)
                val bitmap = Bitmap.createBitmap(
                    (width / sample).coerceAtLeast(1),
                    (height / sample).coerceAtLeast(1),
                    Bitmap.Config.ARGB_8888,
                )
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, bitmap.width, bitmap.height)
                drawable.draw(canvas)
                if (bitmap.hasVisibleColor()) {
                    return bitmap
                }
            }
            return decodeAppWallpaperCache(context)
        }

        private fun readWallpaperBitmap(wm: WallpaperManager): Bitmap? {
            return try {
                val method = WallpaperManager::class.java.getMethod("getBitmap")
                (method.invoke(wm) as? Bitmap)?.takeIf { it.width > 0 }
            } catch (se: SecurityException) {
                Log.w(TAG, "getBitmap blocked: ${se.message}")
                null
            } catch (t: Throwable) {
                val cause = t.cause
                if (cause is SecurityException) {
                    Log.w(TAG, "getBitmap blocked: ${cause.message}")
                } else {
                    Log.w(TAG, "getBitmap failed: ${t.message}")
                }
                null
            }
        }

        private fun decodeAppWallpaperCache(context: Context): Bitmap? {
            return runCatching {
                val file = java.io.File(context.filesDir, WALLPAPER_CACHE_NAME)
                if (!file.isFile || file.length() < 64L) {
                    return null
                }
                val options = BitmapFactory.Options().apply {
                    inSampleSize = 2
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                BitmapFactory.decodeFile(file.absolutePath, options)
            }.getOrNull()
        }

        private fun decodeWallpaperFile(manager: WallpaperManager): Bitmap? {
            return try {
                manager.getWallpaperFile(WallpaperManager.FLAG_SYSTEM)?.use { file ->
                    val options = BitmapFactory.Options().apply {
                        inSampleSize = 2
                        inPreferredConfig = Bitmap.Config.ARGB_8888
                    }
                    BitmapFactory.decodeFileDescriptor(file.fileDescriptor, null, options)
                }
            } catch (se: SecurityException) {
                Log.w(TAG, "getWallpaperFile blocked: ${se.message}")
                null
            } catch (t: Throwable) {
                Log.w(TAG, "getWallpaperFile failed: ${t.message}")
                null
            }
        }

        private fun sampleSize(width: Int, height: Int): Int {
            val maxEdge = maxOf(width, height).coerceAtLeast(1)
            var sample = 1
            while (maxEdge / sample > MAX_WALLPAPER_EDGE_PX) {
                sample *= 2
            }
            return sample
        }

        private fun Bitmap.hasVisibleColor(): Boolean {
            if (width < 2 || height < 2) {
                return false
            }
            val samples = intArrayOf(
                getPixel(width / 2, height / 2),
                getPixel(width / 4, height / 4),
                getPixel(width * 3 / 4, height * 3 / 4),
            )
            return samples.any { pixel ->
                Color.red(pixel) + Color.green(pixel) + Color.blue(pixel) > 48
            }
        }
    }
}
