package xyz.hafemann.slhserver.module.global

import io.github.togar2.pvp.feature.CombatFeatures
import io.github.togar2.pvp.feature.FeatureType
import io.github.togar2.pvp.feature.provider.DifficultyProvider
import io.github.togar2.pvp.utils.CombatVersion
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.Player
import net.minestom.server.entity.damage.DamageType
import net.minestom.server.entity.metadata.projectile.ArrowMeta
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.event.entity.EntityAttackEvent
import net.minestom.server.event.entity.EntityDamageEvent
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent
import net.minestom.server.event.item.ItemUsageCompleteEvent
import net.minestom.server.item.ItemComponent
import net.minestom.server.world.Difficulty
import xyz.hafemann.slhserver.event.GameStateChangedEvent
import xyz.hafemann.slhserver.game.Game
import xyz.hafemann.slhserver.game.GameState
import xyz.hafemann.slhserver.module.GameModule

class PvPModule : GameModule() {
    val featureSet = CombatFeatures.getVanilla(CombatVersion.MODERN, DifficultyProvider.DEFAULT)
        .remove(FeatureType.ITEM_DAMAGE).add(CombatFeatures.VANILLA_ENCHANTMENT).build()

    override fun initialize(parent: Game, eventNode: EventNode<Event>) {
        MinecraftServer.setDifficulty(Difficulty.EASY)

        val pvpNode = featureSet.createNode()

        // Only activate PVP when the game is running
        eventNode.addListener(GameStateChangedEvent::class.java) { event ->
            if (event.newState == GameState.RUNNING) {
                eventNode.addChild(pvpNode)
            } else {
                eventNode.removeChild(pvpNode)
            }
        }
        // Remove fall damage when in fly mode
        eventNode.addListener(EntityDamageEvent::class.java) { event ->
            if (event.entity !is Player) return@addListener
            val player = event.entity as Player

            if (event.damage.type == DamageType.FALL && player.isAllowFlying) {
                event.isCancelled = true
            }
        }
        // Decrease arrow damage
        eventNode.addListener(ProjectileCollideWithEntityEvent::class.java) { event ->
            if (event.entity.entityType != EntityType.ARROW) return@addListener
            (event.entity.entityMeta as ArrowMeta).isCritical = false
        }

        //eventNode.addListener(EntityDamageEvent::class.java) { event ->
        //    if (event.entity !is Player) return@addListener
        //    if (event.damage.attacker !is Player) return@addListener
        //
        //
        //}
    }
}