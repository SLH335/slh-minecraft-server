package xyz.hafemann.slhserver.command

import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeVec2
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeVec3
import xyz.hafemann.slhserver.util.sendTranslated

class TeleportCommand(name: String, vararg aliases: String) : SLHCommand(name, aliases, block = {
    usage("[<players>] <location>|<destination> [<rotation>]")

    val targetsArg = ArgumentEntity("players").onlyPlayers(true)
    val destinationArg = ArgumentEntity("destination").onlyPlayers(true).singleEntity(true)
    val locationArg = ArgumentRelativeVec3("location")
    val rotationArg = ArgumentRelativeVec2("rotation")

    // tp <location>
    syntax(locationArg) {
        val pos = getPos(locationArg)

        player.teleport(pos)
        sender.sendTranslated("command.teleport.other", player, pos)
    }.requirePlayer()

    // tp <location> <rotation>
    syntax(locationArg, rotationArg) {
        val pos = getPos(locationArg, rotationArg)

        player.teleport(pos)
        sender.sendTranslated("command.teleport.other", player, pos)
    }.requirePlayer()

    // tp <destination>
    syntax(destinationArg) {
        val destination = getFirstPlayer(destinationArg)

        player.teleport(destination.position)
        sender.sendTranslated("command.teleport.other", player, destination)
    }.requirePlayer()

    // tp <players> <location>
    syntax(targetsArg, locationArg) {
        val targets = getPlayers(targetsArg)
        val pos = getPos(locationArg)

        val affectedTargets = forEachIfNotImmune(targets) { target ->
            target.teleport(pos)
            if (target != sender) {
                target.sendTranslated("command.teleport.self", pos)
            }
        }
        if (affectedTargets.isNotEmpty()) {
            sender.sendTranslated("command.teleport.other", affectedTargets, pos)
        }
    }

    // tp <players> <location> <rotation>
    syntax(targetsArg, locationArg, rotationArg) {
        val targets = getPlayers(targetsArg)
        val pos = getPos(locationArg, rotationArg)

        val affectedTargets = forEachIfNotImmune(targets) { target ->
            target.teleport(pos)
            if (target != sender) {
                target.sendTranslated("command.teleport.self", pos)
            }
        }
        if (affectedTargets.isNotEmpty()) {
            sender.sendTranslated("command.teleport.other", affectedTargets, pos)
        }
    }

    // tp <players> <destination>
    syntax(targetsArg, destinationArg) {
        val targets = getPlayers(targetsArg)
        val destination = getFirstPlayer(destinationArg)

        val affectedTargets = forEachIfNotImmune(targets) { target ->
            target.teleport(destination.position)
            if (target != sender) {
                target.sendTranslated("command.teleport.self", destination)
            }
        }
        if (affectedTargets.isNotEmpty()) {
            sender.sendTranslated("command.teleport.other", affectedTargets, destination)
        }
    }
})