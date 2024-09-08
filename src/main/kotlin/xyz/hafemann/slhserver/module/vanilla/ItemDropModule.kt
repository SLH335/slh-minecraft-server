package xyz.hafemann.slhserver.module.vanilla

import net.kyori.adventure.key.Key
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.ItemEntity
import net.minestom.server.entity.Player
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.event.item.ItemDropEvent
import net.minestom.server.event.item.PickupItemEvent
import net.minestom.server.event.player.PlayerBlockBreakEvent
import net.minestom.server.item.ItemStack
import xyz.hafemann.slhserver.game.Game
import xyz.hafemann.slhserver.module.GameModule
import xyz.hafemann.slhserver.util.addItemStackFirstOffHand
import java.time.Duration

class ItemDropModule(
    val dropItemOnBlockBreak: Boolean = true,
    val allowItemDrop: Boolean = true,
    val allowItemPickup: Boolean = true,
    val exceptions: List<Key> = listOf(),
) : GameModule() {
    override fun initialize(parent: Game, eventNode: EventNode<Event>) {
        if (dropItemOnBlockBreak) {
            eventNode.addListener(PlayerBlockBreakEvent::class.java) { event ->
                val material = event.block.registry().material()
                if (material == null || event.player.gameMode == GameMode.CREATIVE) {
                    return@addListener
                }
                if (exceptions.contains(material.key())) return@addListener
                val itemStack = ItemStack.of(material)
                val itemEntity = ItemEntity(itemStack)
                itemEntity.setInstance(event.instance, event.blockPosition.add(0.5, 0.5, 0.5))
                itemEntity.setPickupDelay(Duration.ofMillis(500))
            }
        }

        eventNode.addListener(ItemDropEvent::class.java) { event ->
            if (allowItemDrop) {
                val itemEntity = ItemEntity(event.itemStack)
                if (exceptions.contains(event.itemStack.material().key())) return@addListener
                itemEntity.setInstance(event.instance, event.player.position.add(0.0, 1.5, 0.0))
                itemEntity.velocity = event.player.position.direction().mul(6.0)
                itemEntity.setPickupDelay(Duration.ofSeconds(2))
            } else {
                event.isCancelled = true
            }
        }

        eventNode.addListener(PickupItemEvent::class.java) { event ->
            if (allowItemPickup) {
                if (exceptions.contains(event.itemStack.material().key())) return@addListener
                val player = event.livingEntity as? Player ?: return@addListener
                player.inventory.addItemStackFirstOffHand(event.itemStack)
            } else {
                event.isCancelled = true
            }
        }
    }
}