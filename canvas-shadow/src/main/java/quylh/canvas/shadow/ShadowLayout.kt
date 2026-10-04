package quylh.canvas.shadow

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import android.util.AttributeSet
import android.widget.FrameLayout
import quylh.canvas.shadow.R
import kotlin.math.ceil
import kotlin.math.max

/** A FrameLayout that reserves enough internal room to render a software shadow without clipping. */
class ShadowLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val renderer = CanvasShadowRenderer(this)
    private val contentBounds = RectF()
    private var userPadding = intArrayOf(paddingLeft, paddingTop, paddingRight, paddingBottom)
    private var applyingShadowPadding = false

    var shadowConfig: ShadowConfig = readConfig(attrs, defStyleAttr)
        set(value) {
            field = value.sanitized()
            applyCombinedPadding()
            renderer.invalidateMask()
        }

    init {
        setWillNotDraw(false)
        shadowConfig = shadowConfig.sanitized()
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        if (!applyingShadowPadding) userPadding = intArrayOf(left, top, right, bottom)
        applyCombinedPadding()
    }

    override fun onDraw(canvas: Canvas) {
        updateContentBounds()
        renderer.draw(canvas, shadowConfig, contentBounds)
        super.onDraw(canvas)
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        invalidate()
    }

    override fun onDetachedFromWindow() {
        renderer.release()
        super.onDetachedFromWindow()
    }

    fun setShadowColor(color: Int) {
        shadowConfig = shadowConfig.copy(color = ColorStateList.valueOf(color))
    }

    fun setShadowColor(colors: ColorStateList) {
        shadowConfig = shadowConfig.copy(color = colors)
    }

    fun dp(value: Float): Float = value * resources.displayMetrics.density

    private fun applyCombinedPadding() {
        val edge = shadowInsets()
        applyingShadowPadding = true
        super.setPadding(
            userPadding[0] + edge[0], userPadding[1] + edge[1],
            userPadding[2] + edge[2], userPadding[3] + edge[3],
        )
        applyingShadowPadding = false
    }

    private fun updateContentBounds() {
        val edge = shadowInsets()
        contentBounds.set(
            edge[0].toFloat(), edge[1].toFloat(),
            (width - edge[2]).toFloat(), (height - edge[3]).toFloat(),
        )
    }

    private fun shadowInsets(): IntArray {
        val c = shadowConfig
        if (c.mode == ShadowMode.INNER) return intArrayOf(0, 0, 0, 0)
        val blurExtent = 1.5f * c.blur + c.spread + 2f
        return intArrayOf(
            ceil(max(0f, blurExtent - c.offsetX)).toInt(),
            ceil(max(0f, blurExtent - c.offsetY)).toInt(),
            ceil(max(0f, blurExtent + c.offsetX)).toInt(),
            ceil(max(0f, blurExtent + c.offsetY)).toInt(),
        )
    }

    private fun readConfig(attrs: AttributeSet?, defStyleAttr: Int): ShadowConfig {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ShadowLayout, defStyleAttr, 0)
        return try {
            ShadowConfig(
                color = a.getColorStateList(R.styleable.ShadowLayout_shadowColor)
                    ?: ColorStateList.valueOf(Color.TRANSPARENT),
                alpha = a.getFloat(R.styleable.ShadowLayout_shadowAlpha, 1f),
                blur = a.getDimension(R.styleable.ShadowLayout_shadowBlur, 0f),
                offsetX = a.getDimension(R.styleable.ShadowLayout_shadowOffsetX, 0f),
                offsetY = a.getDimension(R.styleable.ShadowLayout_shadowOffsetY, 0f),
                cornerRadius = a.getDimension(R.styleable.ShadowLayout_shadowCornerRadius, 0f),
                spread = a.getDimension(R.styleable.ShadowLayout_shadowSpread, 0f),
                mode = when (a.getInt(R.styleable.ShadowLayout_shadowMode, 0)) {
                    1 -> ShadowMode.INNER
                    2 -> ShadowMode.BOTH
                    else -> ShadowMode.OUTER
                },
            )
        } finally {
            a.recycle()
        }
    }

    private fun ShadowConfig.sanitized() = copy(
        alpha = alpha.coerceIn(0f, 1f),
        blur = blur.coerceAtLeast(0f),
        cornerRadius = cornerRadius.coerceAtLeast(0f),
        spread = spread.coerceAtLeast(0f),
    )
}
