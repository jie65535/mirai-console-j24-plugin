package top.jie65535.j24

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.getGroupOrNull
import net.mamoe.mirai.contact.Group

object PluginCommand : CompositeCommand(
    PluginMain, "j24"
) {
    @SubCommand
    suspend fun CommandSender.enable(group: Group? = getGroupOrNull()) {
        if (group == null) {
            sendMessage("必须指定群")
            return
        }
        PluginConfig.enabledGroups.add(group.id)
        sendMessage("OK")
    }

    @SubCommand
    suspend fun CommandSender.disable(group: Group? = getGroupOrNull()) {
        if (group == null) {
            sendMessage("必须指定群")
            return
        }
        PluginConfig.enabledGroups.remove(group.id)
        sendMessage("OK")
    }
}