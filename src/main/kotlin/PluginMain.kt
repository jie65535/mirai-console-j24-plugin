package top.jie65535.j24

import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.utils.info
import java.text.DecimalFormat
import java.time.*
import kotlin.math.max

object PluginMain : KotlinPlugin(
    JvmPluginDescription(
        id = "top.jie65535.j24",
        name = "J 24点游戏",
        version = "1.0.0"
    ) {
        author("jie65535")
        info("24点游戏")
    }
) {
    private val games: MutableMap<Long, Point24> = mutableMapOf()

    private val df = DecimalFormat("0.00")

    private const val prefix = "="
    private const val maxRound = 10

    class GroupState(
        var lastGameTimes: Int = 1,

        /**
         * 游戏计时器
         * 为了防止频繁刷屏，记录开始时间，一定间隔后才可再次触发
         */
        var lastStartedAt: LocalDateTime = LocalDateTime.MIN,

        /**
         * 排行榜计时器
         * 为了防止频繁刷屏，记录出榜时间，一定间隔后才可再次触发
         */
        var lastRankedAt: LocalDateTime = LocalDateTime.MIN
    )

    private val gameStats = mutableMapOf<Long, GroupState>()


    override fun onEnable() {
        logger.info { "Plugin loaded" }
        PluginCommand.register()
        PluginConfig.reload()
        PluginData.reload()

        GlobalEventChannel.parentScope(this)
            .filter { it is GroupMessageEvent && PluginConfig.enabledGroups.contains(it.group.id) }
            .subscribeGroupMessages {
                "24点" reply {
                    var game = games[group.id]
                    val state = gameStats.getOrPut(group.id) { GroupState() }

                    if (state.lastStartedAt.plusMinutes(10).isBefore(LocalDateTime.now())) {
                        state.lastStartedAt = LocalDateTime.now()
                        state.lastGameTimes = 0
                    }

                    if (state.lastGameTimes >= maxRound) {
                        "您参与的太过频繁了，为避免影响他人聊天体验，请稍后再试~"
                    } else if (game == null || game.time.plusMinutes(1) < LocalDateTime.now()) {
                        game = Point24()
                        games[group.id] = game
                        state.lastGameTimes++
                        "{${state.lastGameTimes}/$maxRound} 请用 $game 组成结果为24的算式，以'$prefix'开头验证"
                    } else Unit
                }

                "24点榜" reply aaa@{
                    val g = PluginData.stats[group.id] ?: return@aaa Unit
                    val state = gameStats.getOrPut(group.id) { GroupState() }
                    // 一小时内仅可查询一次
                    if (state.lastRankedAt.plusHours(1).isBefore(LocalDateTime.now())) {
                        // 记录查询时间
                        state.lastRankedAt = LocalDateTime.now()
                        // 拼接排行榜
                        val sb = StringBuilder()
                        sb.appendLine("[均时榜]")
                        sb.append(g.entries.filter { it.value.avgTime != 0.0 }.sortedBy { it.value.avgTime }.take(3).joinToString("\n") {
                            "${df.format(it.value.avgTime)}s | ${group[it.key]?.nameCardOrNick ?: "侠名"}"
                        }).appendLine().appendLine()
                        sb.appendLine("[速度榜]")
                        sb.append(g.entries.sortedBy { it.value.minTime }.take(3).joinToString("\n") {
                            "${df.format(it.value.minTime)}s | ${group[it.key]?.nameCardOrNick ?: "侠名"}"
                        }).appendLine().appendLine()
                        sb.appendLine("[答题榜]")
                        sb.append(g.entries.sortedByDescending { it.value.totalCount }.take(3).joinToString("\n") {
                            "${it.value.totalCount} 道 | ${group[it.key]?.nameCardOrNick ?: "侠名"}"
                        })
                        sb.toString()
                    } else Unit
                }

                startsWith(prefix) quoteReply {
                    val game = games[group.id]
                    if (game != null) {
                        try {
                            val now = LocalDateTime.now()
                            val result = game.evaluate(message.contentToString().removePrefix(prefix).trim())
                            if (result == 24.0) {
                                val duration = Duration.between(game.time, now)
//                                    Instant.ofEpochSecond(
//                                        this.time.toLong())
//                                        .atZone(ZoneId.systemDefault())
//                                        .toLocalDateTime())

                                // 群
                                var g = PluginData.stats[group.id]
                                if (g == null) {
                                    g = mutableMapOf()
                                    PluginData.stats[group.id] = g
                                }
                                // 玩家
                                var stat = g[sender.id]
                                if (stat == null) {
                                    stat = PlayerStat()
                                    g[sender.id] = stat
                                }
                                // 答题数增加
                                stat.totalCount += 1
                                // 用时
                                val t = duration.seconds + duration.nano / 1000000000.0
                                if (stat.minTime > t) {
                                    stat.minTime = t
                                }
                                // 仅统计一定时间内的均值
                                val resultText = if (t < 60) {
                                    // 计数增加
                                    stat.count += 1
                                    // 更新均值
                                    stat.avgTime += (t - stat.avgTime) / max(20, stat.count)

                                    "答对了！用时:${df.format(t)}s 平均:${df.format(stat.avgTime)}s 最快:${df.format(stat.minTime)}s\n"
                                } else {
                                    "回答正确！"
                                }

                                val state = gameStats[group.id]!!
                                val nextTip = if (state.lastGameTimes >= maxRound) {
                                    games.remove(group.id)
                                    "本轮游戏已经结束，感谢参与，请稍作休息~"
                                } else {
                                    state.lastGameTimes++
                                    val newGame = Point24()
                                    games[group.id] = newGame
                                    "{${state.lastGameTimes}/$maxRound}下一题：$newGame"
                                }

                                resultText + nextTip
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
