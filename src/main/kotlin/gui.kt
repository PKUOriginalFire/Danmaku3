package cc.wybxc

import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.application.Application
import javafx.application.Platform
import javafx.beans.value.WritableValue
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.stage.Screen
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.util.Duration
import java.util.concurrent.BlockingQueue
import kotlin.concurrent.thread
import kotlin.concurrent.timer

var danmaQueue: BlockingQueue<DanmakuMessage>? = null

class DanmakuApplication : Application() {
    private val pane = Pane()
    private val stage = Stage()

    override fun start(primaryStage: Stage) {
        // 隐藏任务栏图标
        with(primaryStage) {
            initStyle(StageStyle.UTILITY)
            opacity = 0.0
            width = 0.0
            height = 0.0
            show()
        }

        // 全屏置顶显示
        with(stage) {
            title = "Danmaku!"

            initOwner(primaryStage)
            initStyle(StageStyle.TRANSPARENT)
            isAlwaysOnTop = true
            isMaximized = true

            scene = Scene(pane).apply {
                fill = null
            }

            Screen.getPrimary().visualBounds.let {
                x = it.minX
                y = it.minY
                width = it.width
                height = it.height
            }

            setOnCloseRequest {
                it.consume() // 阻止窗口被关闭
            }
        }

        timer(period = 100) { loadDanmaku() }

        stage.show()
    }

    private fun loadDanmaku() {
        while (danmaQueue?.isNotEmpty() == true) {
            danmaQueue?.poll()?.let {
                Platform.runLater { emitDanmaku(it) }
            }
        }
    }

    private fun emitDanmaku(danmaku: DanmakuMessage) {
        val text = Text(danmaku.text).apply {
            font = Font.font(danmaku.size)
            fill = danmaku.webColor
            when (danmaku.position) {
                DanmakuPosition.Scroll -> {
                    x = stage.width
                    y = layoutBounds.height
                }
                DanmakuPosition.Top -> {
                    x = stage.width / 2 - layoutBounds.width / 2
                    y = layoutBounds.height
                }
                DanmakuPosition.Bottom -> {
                    x = stage.width / 2 - layoutBounds.width / 2
                    y = stage.height - layoutBounds.height
                }
            }
        }
        pane.children.add(text)
        val timeline = Timeline().apply {
            addKeyFrame(stage.width / danmaku.speed, text.xProperty(), -text.layoutBounds.width)
            onFinished = EventHandler<ActionEvent> {
                pane.children.remove(text)
            }
        }
        timeline.play()
    }


}

fun <T> Timeline.addKeyFrame(durationSeconds: Double, property: WritableValue<T>, endValue: T) {
    keyFrames.add(
        KeyFrame(
            Duration.seconds(durationSeconds),
            KeyValue(property, endValue)
        )
    )
}

var uiThread: Thread? = null

fun startGUI(queue: BlockingQueue<DanmakuMessage>) {
    uiThread = thread(start = true, name = "danmaku") {
        try {
            danmaQueue = queue
            Application.launch(DanmakuApplication::class.java)
        } catch (e: RuntimeException) {
        }
    }
}

fun stopGUI() {
    uiThread?.interrupt()
}