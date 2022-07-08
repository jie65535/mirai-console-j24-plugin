package top.jie65535.j24

import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.utils.info

object PluginMain : KotlinPlugin(
    JvmPluginDescription(
        id = "top.jie65535.j24",
        name = "J 24点游戏",
        version = "0.1.1"
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
        eventChannel.subscribeGroupMessages {
            startsWith("24点") and content { PluginConfig.enabledGroups.contains(this.group.id) } quoteReply {
                val game = Point24()
                games[this.sender.id] = game
                "请用 [${game.points[0]}] [${game.points[1]}] [${game.points[2]}] [${game.points[3]}] 组成结果为24的算式，以'$prefix'开头验证"
            }

            startsWith(prefix) and content { PluginConfig.enabledGroups.contains(this.group.id) } quoteReply {
                val game = games[sender.id]
                if (game == null) {
                    "你还没有抽数字哦，说“24点”来开始游戏吧"
                } else {
                    try {
                        val result = game.evaluate(message.contentToString().removePrefix(prefix).trim())
                        if (result == 24.0) {
                            games.remove(sender.id)
                            "厉害，答对了！"
                        } else {
                            "答错了，计算结果为 $result"
                        }
                    } catch (e: Throwable) {
//                        logger.error(e)
                        "错误：${e.message}"
                    }
                }
            }
        }
    }
}
