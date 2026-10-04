package quylh.canvas.shadow

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.view.View
import kotlin.math.roundToInt

/** Builds the expensive blur mask once, then recolors it for drawable-state changes. */
internal class CanvasShadowRenderer(private val view: View) {
    private val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val bounds = RectF()
    private var bitmap: Bitmap? = null
    private var cachedWidth = 0
    private var cachedHeight = 0
    private var cachedColor = Color.TRANSPARENT
    private var cachedConfig: ShadowConfig? = null

    fun draw(canvas: Canvas, config: ShadowConfig, contentBounds: RectF) {
        val color = config.color.getColorForState(view.drawableState, config.color.defaultColor)
        val shadowColor = (color and 0x00ffffff) or
            ((Color.alpha(color) * config.alpha.coerceIn(0f, 1f)).roundToInt() shl 24)
        if (view.width <= 0 || view.height <= 0 || Color.alpha(shadowColor) == 0) return

        if (bitmap == null || cachedWidth != view.width || cachedHeight != view.height ||
            cachedConfig != config || bounds != contentBounds
        ) {
            buildMask(config, contentBounds)
        }
        if (cachedColor != shadowColor) recolor(shadowColor)
        bitmap?.let { canvas.drawBitmap(it, 0f, 0f, bitmapPaint) }
    }

    private fun buildMask(config: ShadowConfig, contentBounds: RectF) {
        release()
        val image = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888).apply {
            density = Bitmap.DENSITY_NONE
        }
        val canvas = Canvas(image)
        val blurRadius = if (config.blur > 0f) {
            (((config.blur / 2f) - .5f) / .57735f).coerceAtLeast(.01f)
        } else 0f

        if (config.mode != ShadowMode.INNER) drawOuterShadow(canvas, config, contentBounds, blurRadius)
        if (config.mode != ShadowMode.OUTER) drawInnerShadow(canvas, config, contentBounds, blurRadius)
        bitmap = image
        cachedWidth = view.width
        cachedHeight = view.height
        cachedConfig = config
        bounds.set(contentBounds)
        cachedColor = Color.TRANSPARENT
    }

    private fun drawOuterShadow(canvas: Canvas, config: ShadowConfig, contentBounds: RectF, blurRadius: Float) {
        val rect = RectF(contentBounds).apply { inset(-config.spread, -config.spread) }
        if (blurRadius > 0f) {
            maskPaint.color = Color.TRANSPARENT
            maskPaint.setShadowLayer(blurRadius, config.offsetX, config.offsetY, MASK_COLOR)
            canvas.drawRoundRect(rect, config.cornerRadius, config.cornerRadius, maskPaint)
            maskPaint.clearShadowLayer()
        } else {
            maskPaint.color = MASK_COLOR
            rect.offset(config.offsetX, config.offsetY)
            canvas.drawRoundRect(rect, config.cornerRadius, config.cornerRadius, maskPaint)
        }
    }

    /** Casts the shadow of an inverse shape into its rounded-rectangle hole. */
    private fun drawInnerShadow(canvas: Canvas, config: ShadowConfig, contentBounds: RectF, blurRadius: Float) {
        val inner = RectF(contentBounds).apply { inset(config.spread, config.spread) }
        if (inner.isEmpty) return
        val inverse = Path().apply {
            fillType = Path.FillType.EVEN_ODD
            addRect(-view.width.toFloat(), -view.height.toFloat(), view.width * 2f, view.height * 2f, Path.Direction.CW)
            addRoundRect(inner, config.cornerRadius, config.cornerRadius, Path.Direction.CW)
        }
        val clip = Path().apply {
            addRoundRect(inner, config.cornerRadius, config.cornerRadius, Path.Direction.CW)
        }
        canvas.save()
        canvas.clipPath(clip)
        maskPaint.color = MASK_COLOR
        if (blurRadius > 0f) {
            maskPaint.setShadowLayer(blurRadius, config.offsetX, config.offsetY, MASK_COLOR)
        } else {
            inverse.offset(config.offsetX, config.offsetY)
        }
        canvas.drawPath(inverse, maskPaint)
        maskPaint.clearShadowLayer()
        canvas.restore()
    }

    private fun recolor(color: Int) {
        bitmapPaint.colorFilter = ColorMatrixColorFilter(floatArrayOf(
            0f, 0f, 0f, 0f, Color.red(color).toFloat(),
            0f, 0f, 0f, 0f, Color.green(color).toFloat(),
            0f, 0f, 0f, 0f, Color.blue(color).toFloat(),
            0f, 0f, 0f, Color.alpha(color) / 254f, 0f,
        ))
        cachedColor = color
    }

    fun invalidateMask() {
        cachedConfig = null
        view.invalidate()
    }

    fun release() {
        bitmap = null
        cachedWidth = 0
        cachedHeight = 0
    }

    private companion object {
        val MASK_COLOR: Int = Color.argb(254, 0, 0, 0)
    }
}
