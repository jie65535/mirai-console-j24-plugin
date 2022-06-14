package top.jie65535.j24

import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.enable
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.load
import net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader

suspend fun main() {
    MiraiConsoleTerminalLoader.startAsDaemon()

    PluginMain.load()
    PluginMain.enable()

//    val bot = MiraiConsole.addBot(123456, "") {
//        fileBasedDeviceInfo()
//    }.alsoLogin()

    MiraiConsole.job.join()
}

fun test() {
    val game = Point24()
    println(game.points)
    println(game.evaluate("${game.points[0]}+${game.points[1]}+${game.points[2]}+${game.points[3]}"))
}