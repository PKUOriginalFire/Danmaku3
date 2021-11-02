package cc.wybxc

import javafx.scene.paint.Color

enum class DanmakuPosition {
    Scroll, Top, Bottom
}

data class DanmakuMessage(
    val text: String, val size: Double = 25.0, val color: String = "#FFF",
    val position: DanmakuPosition = DanmakuPosition.Scroll, val speed: Double = 144.0
) {
    val webColor: Color
        get() {
            return try {
                Color.web(color)
            } catch (_: IllegalArgumentException) {
                Color.WHITE
            }
        }
}