package tech.hookin.challengor.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/** Brand palette of Challengor. */
object ChallengorColors {
    val Sage = Color(0xFFC5CDB6)
    val ForestGreen = Color(0xFF1F2C13)
    val NearBlack = Color(0xFF000702)
    val Rust = Color(0xFFA94802)
    val White = Color(0xFFFFFFFF)

    /** Tints between the brand colours, for layered surfaces (cards, bars, selected items). */
    val ForestDeep = lerp(NearBlack, ForestGreen, 0.55f)
    val ForestLight = lerp(ForestGreen, Sage, 0.18f)
    val ForestLighter = lerp(ForestGreen, Sage, 0.32f)
}
