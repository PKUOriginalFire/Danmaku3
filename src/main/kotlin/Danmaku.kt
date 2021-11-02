package cc.wybxc

import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.utils.info
import java.util.concurrent.LinkedBlockingQueue

object Danmaku : KotlinPlugin(
    JvmPluginDescription(
        id = "cc.wybxc.danmaku",
        name = "弹幕姬",
        version = "1.0-SNAPSHOT",
    ) {
        author("忘忧北萱草")
    }
) {
    override fun onEnable() {
        logger.info { "弹幕姬加载成功desu~" }
        startGUI(queue)

        val eventChannel = GlobalEventChannel.parentScope(this)
        eventChannel.subscribeAlways<FriendMessageEvent> {
            emitDanmaku(DanmakuMessage(text = message.contentToString()))
        }

        CommandManager.registerCommand(DanmakuCommand)
    }

    override fun onDisable() {
        stopGUI()
    }


    private val queue = LinkedBlockingQueue<DanmakuMessage>()

    fun emitDanmaku(danmaku: DanmakuMessage) {
        queue.add(danmaku)
    }

    object DanmakuCommand : SimpleCommand(
        Danmaku, "danmaku", "弹幕",
        description = "发送一条弹幕"
    ) {
        @Handler // 标记这是指令处理器  // 函数名随意
        suspend fun CommandSender.handle(text: String) { // 这两个参数会被作为指令参数要求
            emitDanmaku(DanmakuMessage(text = text))
        }
    }
}