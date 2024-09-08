package xyz.hafemann.slhserver.module.minigame

import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.event.item.ItemDropEvent
import net.minestom.server.event.item.PickupItemEvent
import xyz.hafemann.slhserver.game.Game
import xyz.hafemann.slhserver.module.GameModule
import java.util.UUID

class SpectatorModule : GameModule() {
    val spectatingPlayers = mutableListOf<UUID>()
    override fun initialize(parent: Game, eventNode: EventNode<Event>) {
        eventNode.addListener(PickupItemEvent::class.java) { event ->
            if (spectatingPlayers.contains(event.entity.uuid)) {
                event.isCancelled = true
            }
        }
        eventNode.addListener(ItemDropEvent::class.java) { event ->
            if (spectatingPlayers.contains(event.player.uuid)) {
                event.isCancelled = true
            }
        }
    }

    fun addSpectator(player: Player) {
        if (spectatingPlayers.contains(player.uuid)) return
        spectatingPlayers.add(player.uuid)

        player.gameMode = GameMode.SPECTATOR
        player.updateViewableRule { viewer -> spectatingPlayers.contains(viewer.uuid) }
    }

    fun removeSpectator(player: Player) {
        if (!spectatingPlayers.contains(player.uuid)) return
        spectatingPlayers.remove(player.uuid)

        player.stopSpectating()
        player.clearEffects()
        player.gameMode = GameMode.SURVIVAL
        player.updateViewableRule { true }
    }
}