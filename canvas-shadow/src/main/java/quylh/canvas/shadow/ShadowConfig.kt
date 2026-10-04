package quylh.canvas.shadow

import android.content.res.ColorStateList
import android.graphics.Color

/** Runtime shadow values. Dimensions are pixels; use [ShadowLayout.dp] when needed. */
data class ShadowConfig(
    val color: ColorStateList = ColorStateList.valueOf(Color.TRANSPARENT),
    val alpha: Float = 1f,
    val blur: Float = 0f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val cornerRadius: Float = 0f,
    val spread: Float = 0f,
    val mode: ShadowMode = ShadowMode.OUTER,
)

enum class ShadowMode {
    OUTER,
    INNER,
    BOTH,
}
