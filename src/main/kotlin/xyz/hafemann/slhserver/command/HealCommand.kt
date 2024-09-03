package xyz.hafemann.slhserver.command

import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity
import net.minestom.server.command.builder.arguments.number.ArgumentInteger
import xyz.hafemann.slhserver.util.sendTranslated

class HealCommand(name: String, vararg aliases: String) : SLHCommand(name, aliases, block = {
    usage("[<players>] [<health>]")

    val targetsArg = ArgumentEntity("players").onlyPlayers(true)
    val healthArg = ArgumentInteger("health")

    syntax {
        player.health = 20 + player.additionalHearts
        player.food = 20
        player.foodSaturation = 20f

        sender.sendTranslated("command.heal.self")
    }.requirePlayer()

    syntax(healthArg) {
        val health = get(healthArg)
        player.health += health

        sender.sendTranslated("command.heal.self.amount", health)
    }.requirePlayer()

    syntax(targetsArg) {
        val targets = getPlayers(targetsArg)

        for (target in targets) {
            target.health = 20 + target.additionalHearts
            target.food = 20
            target.foodSaturation = 20f
            if (target != sender) {
                target.sendTranslated("command.heal.self")
            }
        }

        sender.sendTranslated("command.heal.other", targets)
    }

    syntax(targetsArg, healthArg) {
        val targets = getPlayers(targetsArg)
        val health = get(healthArg)

        for (target in targets) {
            target.health += health
            if (target != sender) {
                target.sendTranslated("command.heal.self.amount", health)
            }
        }

        sender.sendTranslated("command.heal.other.amount", targets, health)
    }
})