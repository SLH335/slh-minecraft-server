package xyz.hafemann.slhserver.command

import net.kyori.adventure.text.Component
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.command.builder.suggestion.SuggestionEntry
import net.minestom.server.entity.GameMode
import xyz.hafemann.slhserver.util.sendTranslated

class GamemodeCommand(name: String, vararg aliases: String) : SLHCommand(name, aliases, block = {
    usage("<mode> [targets]")

    val gamemodeArg = ArgumentType.Word("gamemode").setSuggestionCallback { _, context, suggestion ->
        val input: String = context.get("gamemode")

        for (mode in GameMode.entries) {
            val gameMode = mode.name.lowercase()
            if (input.toCharArray()[0].code == 0 || gameMode.startsWith(input)) {
                suggestion.addEntry(SuggestionEntry(gameMode, Component.empty()))
            }
        }
    }

    val targetsArg = ArgumentType.Entity("player").onlyPlayers(true)

    syntax(gamemodeArg) {
        val modeInput = get(gamemodeArg)

        val mode = parseModeString(modeInput)
        if (mode == null) {
            player.sendTranslated("command.gamemode.invalid", modeInput)
            return@syntax
        }

        player.gameMode = mode

        player.sendTranslated("command.gamemode.self", mode)
    }.requirePlayer()

    syntax(gamemodeArg, targetsArg) {
        val modeInput = get(gamemodeArg)
        val targets = getPlayers(targetsArg)

        val mode = parseModeString(modeInput)
        if (mode == null) {
            sender.sendTranslated("command.gamemode.invalid", modeInput)
            return@syntax
        }

        val affectedTargets = forEachIfNotImmune(targets) { target ->
            target.gameMode = mode
            target.sendTranslated("command.gamemode.self", mode)
        }

        if (affectedTargets.size > 1 || affectedTargets.isNotEmpty() && targets[0] != sender) {
            sender.sendTranslated("command.gamemode.other", affectedTargets, mode)
        }
    }.requirePermission("$permission.others")
})

private fun parseModeString(modeInput: String): GameMode? {
    if (modeInput.lowercase().take(2) == "sp") {
        return GameMode.SPECTATOR
    }
    return when (modeInput.lowercase().take(1)) {
        "s", "0" -> GameMode.SURVIVAL
        "c", "1" -> GameMode.CREATIVE
        "a", "2" -> GameMode.ADVENTURE
        "3" -> GameMode.SPECTATOR
        else -> null
    }
}