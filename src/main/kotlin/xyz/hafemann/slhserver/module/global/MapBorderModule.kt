package xyz.hafemann.slhserver.module.global

import net.minestom.server.entity.damage.Damage
import net.minestom.server.entity.damage.DamageType
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.event.item.ItemDropEvent
import net.minestom.server.event.player.PlayerBlockPlaceEvent
import net.minestom.server.event.player.PlayerMoveEvent
import xyz.hafemann.slhserver.game.Game
import xyz.hafemann.slhserver.game.GameState
import xyz.hafemann.slhserver.module.GameModule
import xyz.hafemann.slhserver.module.SoftDependsOn
import xyz.hafemann.slhserver.module.minigame.TeamModule
import xyz.hafemann.slhserver.util.sendTranslatedError

@SoftDependsOn(TeamModule::class)
class MapBorderModule(
    val radius: Int = 120,
    val height: Int = 100,
    val voidHeight: Int = 0,
    val voidBehavior: VoidBehavior = VoidBehavior.DEATH,
) : GameModule() {

    override fun initialize(parent: Game, eventNode: EventNode<Event>) {
        eventNode.addListener(PlayerBlockPlaceEvent::class.java) { event ->
            val player = event.player
            val pos = event.blockPosition

            if (!parent.players.contains(player)) return@addListener

            if (pos.y > height) {
                player.sendTranslatedError("game.error.cannot_build")
                event.isCancelled = true
            }
            if (pos.x > radius || pos.x < -radius || pos.z > radius || pos.z < -radius) {
                player.sendTranslatedError("game.error.cannot_build")
                event.isCancelled = true
            }

            for (spawn in parent.getModule<TeamModule>().teams.map { it.respawnPoint() }) {
                if (pos.distance(spawn) < 8) {
                    player.sendTranslatedError("game.error.cannot_build")
                    event.isCancelled = true
                }
            }
        }

        eventNode.addListener(PlayerMoveEvent::class.java) { event ->
            val player = event.player

            if (player.position.y < voidHeight) {
                if (parent.state == GameState.RUNNING && voidBehavior == VoidBehavior.DEATH
                    && parent.players.contains(player)
                ) {
                    val lastDamager = player.lastDamageSource?.source
                    player.damage(
                        Damage(
                            DamageType.OUT_OF_WORLD,
                            lastDamager,
                            lastDamager,
                            lastDamager?.position,
                            player.health
                        )
                    )
                } else {
                    player.teleport(parent.mapProperties.spawn)
                }
            }
        }

        eventNode.addListener(ItemDropEvent::class.java) { event ->
            if (event.player.position.y < 65) {
                event.isCancelled = true
            }
        }
    }
}

enum class VoidBehavior {
    DEATH,
    TP_SPAWN
}