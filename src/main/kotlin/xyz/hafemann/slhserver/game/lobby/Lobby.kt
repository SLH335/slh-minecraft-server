package xyz.hafemann.slhserver.game.lobby

import net.minestom.server.event.player.PlayerSpawnEvent
import xyz.hafemann.slhserver.game.Game
import xyz.hafemann.slhserver.module.global.*

class Lobby(map: String) : Game("lobby", "lobby", map) {
    override fun initialize() {
        use(ChatModule())
        use(PlayerListModule())
        use(WorldPermissionModule(allowBreakBlocks = false, allowPlaceBlocks = false))
        use(NPCModule())
        use(NametagModule())

        eventNode.addListener(PlayerSpawnEvent::class.java) { event ->
            val player = event.player

        }
    }
}