package xyz.hafemann.slhserver.module.global

import net.kyori.adventure.text.Component
import net.minestom.server.entity.Player
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.event.inventory.InventoryPreClickEvent
import net.minestom.server.inventory.Inventory
import net.minestom.server.inventory.InventoryType
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import xyz.hafemann.slhserver.game.Game
import xyz.hafemann.slhserver.game.GameState
import xyz.hafemann.slhserver.module.GameModule
import xyz.hafemann.slhserver.module.SoftDependsOn
import xyz.hafemann.slhserver.module.minigame.ResourceType
import xyz.hafemann.slhserver.module.minigame.TeamModule
import xyz.hafemann.slhserver.service.gui.GUI

@SoftDependsOn(TeamModule::class)
class GUIModule : GameModule() {
    override fun initialize(parent: Game, eventNode: EventNode<Event>) {
        eventNode.addListener(InventoryPreClickEvent::class.java) { event ->
            if (event.inventory !is GUI) return@addListener
            val gui = event.inventory as GUI
            if (event.inventory?.title != Component.text("Shop")) return@addListener
            event.isCancelled = true
            if (parent.state != GameState.RUNNING) return@addListener

            val player = event.player
            val slot = event.slot

            if (event.slot in gui.clickActions.keys) {
                gui.clickActions[slot]?.invoke(player)
            }
        }
    }

    fun createShop(game: Game, player: Player): Inventory {
        val gui = GUI(InventoryType.CHEST_6_ROW, Component.text("Shop"))

        // Blocks
        gui.setBuyableItem(
            0,
            GUI.teamConcrete(game, player).withAmount(16),
            ResourceType.COPPER, 4
        )
        gui.setBuyableItem(
            1,
            ItemStack.of(Material.OAK_PLANKS).withAmount(16),
            ResourceType.IRON, 4
        )
        gui.setBuyableItem(
            2,
            ItemStack.of(Material.COPPER_BLOCK).withAmount(8),
            ResourceType.COPPER, 4
        )
        gui.setBuyableItem(
            3,
            ItemStack.of(Material.OBSIDIAN).withAmount(4),
            ResourceType.GOLD, 4
        )

        // Tools
        gui.setBuyableItem(
            9,
            ItemStack.of(Material.WOODEN_PICKAXE),
            ResourceType.COPPER, 8
        )
        gui.setBuyableItem(
            10,
            ItemStack.of(Material.WOODEN_AXE),
            ResourceType.COPPER, 8
        )
        gui.setBuyableItem(
            11,
            ItemStack.of(Material.STONE_PICKAXE),
            ResourceType.COPPER, 16
        )
        gui.setBuyableItem(
            12,
            ItemStack.of(Material.STONE_AXE),
            ResourceType.COPPER, 16
        )
        gui.setBuyableItem(
            13,
            ItemStack.of(Material.IRON_PICKAXE),
            ResourceType.IRON, 3
        )
        gui.setBuyableItem(
            14,
            ItemStack.of(Material.IRON_AXE),
            ResourceType.IRON, 3
        )
        gui.setBuyableItem(
            15,
            ItemStack.of(Material.DIAMOND_PICKAXE),
            ResourceType.IRON, 6
        )
        gui.setBuyableItem(
            16,
            ItemStack.of(Material.DIAMOND_AXE),
            ResourceType.IRON, 6
        )

        // Weapons
        //gui.setBuyableItem(
        //    18,
        //    ItemStack.of(Material.WOODEN_SWORD),
        //    ResourceType.COPPER, 8
        //)
        gui.setBuyableItem(
            18,
            ItemStack.of(Material.STONE_SWORD),
            ResourceType.COPPER, 16
        )
        gui.setBuyableItem(
            19,
            ItemStack.of(Material.IRON_SWORD),
            ResourceType.IRON, 6
        )
        gui.setBuyableItem(
            20,
            ItemStack.of(Material.DIAMOND_SWORD),
            ResourceType.GOLD, 4
        )
        //gui.setBuyableItem(
        //    22,
        //    ItemStack.of(Material.BOW),
        //    ResourceType.IRON, 16
        //)
        //gui.setBuyableItem(
        //    23,
        //    ItemStack.of(Material.ARROW).withAmount(4),
        //    ResourceType.IRON, 2
        //)

        // Consumables
        gui.setBuyableItem(
            27,
            ItemStack.of(Material.COOKED_BEEF).withAmount(4),
            ResourceType.COPPER, 4
        )
        gui.setBuyableItem(
            28,
            ItemStack.of(Material.GOLDEN_APPLE),
            ResourceType.IRON, 3
        )
        //gui.setBuyableItem(
        //    29,
        //    ItemStack.of(Material.POTION),
        //    ResourceType.GOLD, 3
        //)

        // Miscellaneous
        gui.setBuyableItem(
            36,
            ItemStack.of(Material.TNT),
            ResourceType.IRON, 4
        )
        //gui.setBuyableItem(
        //    37,
        //    ItemStack.of(Material.ENDER_PEARL),
        //    ResourceType.GOLD, 4
        //)

        return gui
    }
}