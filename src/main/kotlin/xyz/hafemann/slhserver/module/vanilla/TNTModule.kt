package xyz.hafemann.slhserver.module.vanilla

import io.github.togar2.pvp.entity.explosion.TntEntity
import io.github.togar2.pvp.events.ExplosionEvent
import io.github.togar2.pvp.feature.FeatureType
import net.minestom.server.MinecraftServer
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.PlayerBlockPlaceEvent
import net.minestom.server.instance.block.Block
import xyz.hafemann.slhserver.game.Game
import xyz.hafemann.slhserver.module.DependsOn
import xyz.hafemann.slhserver.module.GameModule
import xyz.hafemann.slhserver.module.SoftDependsOn
import xyz.hafemann.slhserver.module.global.PvPModule
import xyz.hafemann.slhserver.module.global.WorldPermissionModule

@DependsOn(PvPModule::class)
@SoftDependsOn(WorldPermissionModule::class)
class TNTModule : GameModule() {
    override fun initialize(parent: Game, eventNode: EventNode<Event>) {
        parent.instance.explosionSupplier = parent.getModule<PvPModule>()
            .featureSet.get(FeatureType.EXPLOSION).explosionSupplier

        eventNode.addListener(ExplosionEvent::class.java) { event ->
            val worldPermissionModule = parent.getModule<WorldPermissionModule>()
            if (!worldPermissionModule.allowBreakBlocks) {
                // disable all block damage if block breaking is not allowed
                event.affectedBlocks.clear()
            } else if (!worldPermissionModule.allowBreakMap) {
                // disable block damage to map if breaking the map is not allowed
                val blockIterator = event.affectedBlocks.iterator()
                while (blockIterator.hasNext()) {
                    val affectedBlock = blockIterator.next()
                    if (worldPermissionModule.playerPlacedBlocks.none { it.sameBlock(affectedBlock) }) {
                        blockIterator.remove()
                    }
                }
            }
        }

        eventNode.addListener(PlayerBlockPlaceEvent::class.java) { event ->
            if (event.block.key() != Block.TNT.key()) return@addListener
            val tntEntity = TntEntity(event.entity)
            tntEntity.setInstance(parent.instance, event.blockPosition.add(0.5, 0.0, 0.5))
            MinecraftServer.getSchedulerManager().scheduleNextTick {
                parent.instance.setBlock(event.blockPosition, Block.AIR)
            }
        }

        //eventNode.addListener(EntityDamageEvent::class.java) { event ->
        //    if (event.damage.type == DamageType.EXPLOSION) {
        //        Audiences.server().sendMessage(Component.text("is explosion damage"))
        //    }
        //    if (event.damage.type == DamageType.PLAYER_EXPLOSION) {
        //        Audiences.server().sendMessage(Component.text("is player explosion damage"))
        //        event.isCancelled = true
        //    }
        //}
    }
}