package top.jie65535.j24

import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.utils.info

object PluginMain : KotlinPlugin(
    JvmPluginDescription(
        id = "top.jie65535.j24",
        name = "J 24点游戏",
        version = "0.1.0"
    ) {
        author("jie65535")
        info("24点游戏")
    }
) {
    private val games: MutableMap<Long, Point24> = mutableMapOf()

    override fun onEnable() {
        logger.info { "Plugin loaded" }
        //配置文件目录 "${dataFolder.absolutePath}/"
        val eventChannel = GlobalEventChannel.parentScope(this)
        eventChannel.subscribeMessages {
            contains("24点") quoteReply {
                val game = Point24()
                games[this.sender.id] = game
                "你抽到了 [${game.points[0]}] [${game.points[1]}] [${game.points[2]}] [${game.points[3]}]\n" +
                    "请用以上四组数字组合成结果为24的算式，以“答”开头验证"
            }
            startsWith("答") quoteReply {
                val game = games[this.sender.id]
                if (game == null) {
                    "你还没有抽数字哦，说“24点”来开始游戏吧"
                } else {
                    try {
                        val result = game.evaluate(message.contentToString().removePrefix("答").trim())
                        if (result == 24.0) {
                            "恭喜你，答对了！"
                        } else {
                            "答错了，计算结果为 $result"
                        }
                    } catch (e: Throwable) {
                        "错误：${e.message}"
                    }
                }
            }
        }
    }
}
