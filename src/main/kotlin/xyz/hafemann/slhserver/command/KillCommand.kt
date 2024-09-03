package xyz.hafemann.slhserver.command

import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity
import xyz.hafemann.slhserver.util.sendTranslated

class KillCommand(name: String, vararg aliases: String) : SLHCommand(name, aliases, block = {
    usage("[players]")

    val targetsArg = ArgumentEntity("players")

    syntax {
        player.kill()
    }.requirePlayer()

    syntax(targetsArg) {
        val targets = getPlayers(targetsArg)

        val affectedTargets = forEachIfNotImmune(targets) { target ->
            target.kill()
        }
        if (affectedTargets.isNotEmpty()) {
            sender.sendTranslated("command.kill.other", affectedTargets)
        }
    }
})