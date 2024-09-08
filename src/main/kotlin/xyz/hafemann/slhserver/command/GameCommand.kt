package xyz.hafemann.slhserver.command

import xyz.hafemann.slhserver.game
import xyz.hafemann.slhserver.game.GameState
import xyz.hafemann.slhserver.util.sendTranslated
import xyz.hafemann.slhserver.util.sendTranslatedError

class GameCommand(name: String, vararg aliases: String) : SLHCommand(name, aliases, block = {
    usage("start")

    subcommand("start") {
        syntax {
            if (game.state != GameState.WAITING) {
                sender.sendTranslatedError("game.error.cannot_start")
                return@syntax
            }
            game.state = GameState.STARTING
        }
    }
})