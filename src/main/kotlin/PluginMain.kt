package top.jie65535.j24

import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.utils.info
import java.time.Duration
import java.time.LocalDateTime

object PluginMain : KotlinPlugin(
    JvmPluginDescription(
        id = "top.jie65535.j24",
        name = "J 24点游戏",
        version = "0.1.2"
    ) {
        author("jie65535")
        info("24点游戏")
    }
) {
    private val games: MutableMap<Long, Point24> = mutableMapOf()

    private const val prefix = "="

    override fun onEnable() {
        logger.info { "Plugin loaded" }
        PluginCommand.register()
        PluginConfig.reload()

        val eventChannel = GlobalEventChannel.parentScope(this)
        eventChannel.filter { it is GroupMessageEvent && PluginConfig.enabledGroups.contains(it.group.id) }.subscribeGroupMessages {
            startsWith("24点") reply {
                var game = games[group.id]
                if (game == null || game.time.plusMinutes(1) < LocalDateTime.now()) {
                    game = Point24()
                    games[group.id] = game
                    "请用 $game 组成结果为24的算式，以'$prefix'开头验证"
                } else Unit
            }

            startsWith(prefix) quoteReply {
                val game = games[group.id]
                if (game != null) {
                    try {
                        val result = game.evaluate(message.contentToString().removePrefix(prefix).trim())
                        if (result == 24.0) {
                            val newGame = Point24()
                            games[group.id] = newGame
                            val duration = Duration.between(game.time, LocalDateTime.now())
                            "答对了！ ${duration.toMinutes()}:${duration.toSecondsPart()}\n下一题：$newGame"
                        } else {
                            "答错了，计算结果为 $result"
                        }
                    } catch (e: Throwable) {
//                        logger.error(e)
                        "错误：${e.message}"
                    }
                } else Unit
            }
        }
    }
}
