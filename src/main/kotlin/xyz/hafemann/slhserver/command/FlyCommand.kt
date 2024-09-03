package xyz.hafemann.slhserver.command

import net.minestom.server.command.builder.arguments.ArgumentBoolean
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity
import xyz.hafemann.slhserver.util.sendTranslated

class FlyCommand(name: String, vararg aliases: String) : SLHCommand(name, aliases, block = {
    usage("[players]")

    val targetsArg = ArgumentEntity("player").onlyPlayers(true)
    val enabledArg = ArgumentBoolean("enabled")

    syntax {
        val enabled = !player.isAllowFlying

        player.isAllowFlying = enabled
        player.isFlying = enabled
        player.sendTranslated(if (enabled) "command.fly.self.flying" else "command.fly.self.not_flying")
    }.requirePlayer()

    syntax(enabledArg) {
        val enabled = get(enabledArg)

        player.isAllowFlying = enabled
        player.isFlying = enabled
        player.sendTranslated(if (enabled) "command.fly.self.flying" else "command.fly.self.not_flying")
    }.requirePlayer()

    syntax(enabledArg, targetsArg) {
        val enabled = get(enabledArg)
        val targets = getPlayers(targetsArg)

        val affectedTargets = forEachIfNotImmune(targets) { target ->
            target.isAllowFlying = enabled
            target.isFlying = enabled
            target.sendTranslated(if (enabled) "command.fly.self.flying" else "command.fly.self.not_flying")
        }
        if (affectedTargets.size > 1 || affectedTargets.isNotEmpty() && targets[0] != sender) {
            sender.sendTranslated(
                if (enabled) "command.fly.other.flying" else "command.fly.other.not_flying",
                affectedTargets
            )
        }
    }
})