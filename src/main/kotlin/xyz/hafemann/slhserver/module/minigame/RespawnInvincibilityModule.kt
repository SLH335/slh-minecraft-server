package xyz.hafemann.slhserver.module.minigame

import io.github.togar2.pvp.events.EntityKnockbackEvent
import net.minestom.server.MinecraftServer
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.event.entity.EntityAttackEvent
import net.minestom.server.event.entity.EntityDamageEvent
import net.minestom.server.event.player.PlayerRespawnEvent
import net.minestom.server.timer.TaskSchedule
import xyz.hafemann.slhserver.game.Game
import xyz.hafemann.slhserver.module.GameModule
import java.util.UUID

class RespawnInvincibilityModule : GameModule() {
    val invinciblePlayers = mutableListOf<UUID>()

    override fun initialize(parent: Game, eventNode: EventNode<Event>) {
        // Add invincibility to respawning players for 5 seconds (+5 seconds respawn time)
        eventNode.addListener(PlayerRespawnEvent::class.java) { event ->
            invinciblePlayers.add(event.player.uuid)

            MinecraftServer.getSchedulerManager().scheduleTask({
                invinciblePlayers.remove(event.player.uuid)
                return@scheduleTask TaskSchedule.stop()
            }, TaskSchedule.seconds(10))
        }
        // Cancel damage events for invincible players
        eventNode.addListener(EntityDamageEvent::class.java) { event ->
            if (event.entity.uuid !in invinciblePlayers) return@addListener
            event.isCancelled = true
        }
        // Cancel knockback events for invincible players
        eventNode.addListener(EntityKnockbackEvent::class.java) { event ->
            if (event.entity.uuid !in invinciblePlayers) return@addListener
            event.isCancelled = true
        }
        // Remove invincibility if invicible player attacks another player
        eventNode.addListener(EntityAttackEvent::class.java) { event ->
            if (event.entity.uuid !in invinciblePlayers) return@addListener
            invinciblePlayers.remove(event.entity.uuid)
        }
    }
}