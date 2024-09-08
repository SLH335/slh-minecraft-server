package xyz.hafemann.slhserver.command

import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity
import xyz.hafemann.slhserver.util.sendTranslated

class ClearCommand(name: String, vararg aliases: String) : SLHCommand(name, aliases, block = {
    usage("[<players>]")

    val targetsArg = ArgumentEntity("players").onlyPlayers(true)

    syntax {
        player.inventory.clear()
        player.sendTranslated("command.clear.self")
    }.requirePlayer()

    syntax(targetsArg) {
        val targets = getPlayers(targetsArg)

        val affectedTargets = forEachIfNotImmune(targets) { target ->
            target.inventory.clear()
            target.sendTranslated("command.clear.self")
        }

        if (affectedTargets.isNotEmpty()) {
            sender.sendTranslated("command.clear.other", affectedTargets)
        }
    }
})