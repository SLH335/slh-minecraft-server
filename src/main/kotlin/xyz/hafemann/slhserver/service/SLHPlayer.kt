package xyz.hafemann.slhserver.service

import io.github.togar2.pvp.player.CombatPlayerImpl
import net.kyori.adventure.text.Component
import net.minestom.server.adventure.audience.Audiences
import net.minestom.server.coordinate.Vec
import net.minestom.server.event.EventDispatcher
import net.minestom.server.event.player.PlayerDeathEvent
import net.minestom.server.event.player.PlayerRespawnEvent
import net.minestom.server.network.player.PlayerConnection
import java.util.*

class SLHPlayer(
    uuid: UUID,
    username: String,
    playerConnection: PlayerConnection,
) : CombatPlayerImpl(uuid, username, playerConnection) {

    override fun kill() {
        this.setVelocity(Vec(0.0))

        // get death message to chat
        var chatMessage = if (lastDamage != null) {
            lastDamage.buildDeathMessage(this)
        } else { // may happen if killed by the server without applying damage
            Component.text("$username died")
        }

        // Call player death event
        val playerDeathEvent = PlayerDeathEvent(this, null, chatMessage)
        EventDispatcher.call(playerDeathEvent)

        chatMessage = playerDeathEvent.chatMessage

        // #buildDeathMessage can return null, check here
        if (chatMessage != null) {
            Audiences.players().sendMessage(chatMessage)
        }

        // Set death location
        if (getInstance() != null) {
            setDeathLocation(getInstance().dimensionName, getPosition())
        }

        respawn()
    }

    override fun respawn() {
        fireTicks = 0
        entityMeta.isOnFire = false
        refreshHealth()

        val respawnEvent = PlayerRespawnEvent(this)
        EventDispatcher.call(respawnEvent)
        refreshIsDead(false)
        updatePose()

        val respawnPosition = respawnEvent.respawnPosition
        teleport(respawnPosition).thenRun { this.refreshAfterTeleport() }
    }
}