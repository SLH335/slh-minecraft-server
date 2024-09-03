package xyz.hafemann.slhserver.game.lobby

import net.minestom.server.event.player.PlayerSpawnEvent
import xyz.hafemann.slhserver.game.Game
import xyz.hafemann.slhserver.module.global.PlayerListModule

class Lobby(map: String) : Game("lobby", "lobby", map) {
    override fun initialize() {
        use(PlayerListModule())

        eventNode.addListener(PlayerSpawnEvent::class.java) { event ->
            val player = event.player

        }
    }
}