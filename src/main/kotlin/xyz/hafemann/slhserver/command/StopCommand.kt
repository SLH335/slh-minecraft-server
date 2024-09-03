package xyz.hafemann.slhserver.command

import net.kyori.adventure.text.Component
import net.minestom.server.MinecraftServer
import xyz.hafemann.slhserver.util.sendTranslated
import kotlin.system.exitProcess

class StopCommand(name: String, vararg aliases: String) : SLHCommand(name, aliases, block = {
    syntax {
        sender.sendTranslated("command.stop.self")
        sender.sendMessage("Stopping server")
        MinecraftServer.getInstanceManager().instances.forEach { instance ->
            instance.players.forEach { player ->
                player.kick(Component.translatable("command.stop.other"))
            }
        }
        MinecraftServer.stopCleanly()
        exitProcess(0)
    }
})