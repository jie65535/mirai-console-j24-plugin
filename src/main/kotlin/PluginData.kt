package top.jie65535.j24

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value

object PluginData : AutoSavePluginData("data") {

    /**
     * 统计数据
     * -群
     *  - 群员
     *   - 数据
     */
    val stats: MutableMap<Long, MutableMap<Long, PlayerStat>> by value()
}

@kotlinx.serialization.Serializable
data class PlayerStat(
    /**
     * 总答题数
     */
    var totalCount: Int = 0,

    /**
     * 统计答题数
     */
    var count: Int = 0,

    /**
     * 平均答题时间(S)
     */
    var avgTime: Double = 0.0,

    /**
     * 最快答题时间(S)
     */
    var minTime: Double = Double.MAX_VALUE,
)