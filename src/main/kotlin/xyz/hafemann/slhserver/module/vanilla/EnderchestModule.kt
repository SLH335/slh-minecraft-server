package xyz.hafemann.slhserver.module.vanilla

import net.kyori.adventure.text.Component
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.event.inventory.InventoryItemChangeEvent
import net.minestom.server.event.player.PlayerBlockInteractEvent
import net.minestom.server.event.player.PlayerBlockPlaceEvent
import net.minestom.server.instance.block.Block
import net.minestom.server.inventory.Inventory
import net.minestom.server.inventory.InventoryType
import xyz.hafemann.slhserver.game.Game
import xyz.hafemann.slhserver.module.GameModule

class EnderchestModule : GameModule() {
    private val enderchests = mutableMapOf<String, Inventory>()

    override fun initialize(parent: Game, eventNode: EventNode<Event>) {
        eventNode.addListener(PlayerBlockInteractEvent::class.java) { event ->
            if (event.block.key() != Block.ENDER_CHEST.key() && event.block.key() != Block.CHEST.key()) return@addListener
            event.isCancelled = true
            val player = event.player

            val inventory = if (enderchests.contains(player.username)) {
                enderchests[player.username]!!
            } else {
                Inventory(InventoryType.CHEST_3_ROW, Component.text("${player.username}'s ").append(Component.translatable("container.enderchest")))
            }
            player.openInventory(inventory)
            enderchests[player.username] = inventory
        }
        eventNode.addListener(InventoryItemChangeEvent::class.java) { event ->
            for (enderchest in enderchests) {
                if (event.inventory?.title != Component.text("${enderchest.key}'s ").append(Component.translatable("container.enderchest"))) {
                    return@addListener
                }
                enderchests[enderchest.key] = event.inventory!!
            }
        }
        eventNode.addListener(PlayerBlockPlaceEvent::class.java) { event ->
            val targetBlockPos = event.player.getTargetBlockPosition(4) ?: return@addListener
            if (event.instance.getBlock(targetBlockPos).key() == Block.ENDER_CHEST.key() || event.instance.getBlock(targetBlockPos).key() == Block.CHEST.key()) {
                event.isCancelled = true
            }
        }
    }
}