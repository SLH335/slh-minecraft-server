package xyz.hafemann.slhserver.module.global

import net.minestom.server.MinecraftServer
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.PlayerDeathEvent
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.event.player.PlayerRespawnEvent
import net.minestom.server.event.player.PlayerSpawnEvent
import xyz.hafemann.slhserver.game.Game
import xyz.hafemann.slhserver.module.GameModule
import xyz.hafemann.slhserver.service.nametag.Nametag

class NametagModule : GameModule() {
    override fun initialize(parent: Game, eventNode: EventNode<Event>) {
        eventNode.addListener(PlayerSpawnEvent::class.java) { event ->
            val player = event.player

            if (event.isFirstSpawn) {
                player.addPassenger(Nametag(player.name, player.uuid))
            }
        }

        eventNode.addListener(PlayerDisconnectEvent::class.java) { event ->
            val player = event.player

            player.passengers.find { it is Nametag && it.id == player.uuid}?.remove()
        }

        eventNode.addListener(PlayerDeathEvent::class.java) { event ->
            val player = event.player

            player.passengers.find { it is Nametag && it.id == player.uuid}?.remove()
        }

        eventNode.addListener(PlayerRespawnEvent::class.java) { event ->
            val player = event.player

            MinecraftServer.getSchedulerManager().scheduleNextTick {
                player.addPassenger(Nametag(player.name, player.uuid))
            }
        }
    }
}