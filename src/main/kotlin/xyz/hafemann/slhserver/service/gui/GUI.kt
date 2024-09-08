package xyz.hafemann.slhserver.service.gui

import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.minestom.server.entity.Player
import net.minestom.server.inventory.Inventory
import net.minestom.server.inventory.InventoryType
import net.minestom.server.inventory.TransactionOption
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.sound.SoundEvent
import xyz.hafemann.slhserver.game.Game
import xyz.hafemann.slhserver.module.minigame.ResourceType
import xyz.hafemann.slhserver.module.minigame.TeamModule
import xyz.hafemann.slhserver.util.addItemStackFirstOffHand
import xyz.hafemann.slhserver.util.sendTranslatedError
import kotlin.math.min

class GUI(
    inventoryType: InventoryType = InventoryType.CHEST_6_ROW,
    title: Component,
) : Inventory(inventoryType, title) {

    val clickActions = mutableMapOf<Int, (Player) -> Unit>()

    fun setClickableItem(slot: Int, itemStack: ItemStack, action: (Player) -> Unit) {
        super.setItemStack(slot, itemStack)
        clickActions[slot] = action
    }

    fun setBuyableItem(slot: Int, itemStack: ItemStack, resource: ResourceType, amount: Int) {
        var displayItem = itemStack.withLore(
            Component.text("Cost: ", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
                .append(Component.text("${amount}x ", resource.color).append(resource.title))
        )
        if (itemStack.amount() > 1) {
            displayItem = displayItem.withCustomName(
                Component.text(itemStack.amount()).append(Component.text("x ")).decoration(TextDecoration.ITALIC, false)
                    .append(Component.translatable(itemStack.material().registry().translationKey()))
            )
        }

        setClickableItem(slot, displayItem) { player ->
            val currencyItems = player.inventory.itemStacks.filter { it.material().key() == resource.material.key() }
            val currency = if (currencyItems.isEmpty()) 0 else {
                currencyItems.map { it.amount() }.reduce { acc, i -> acc + i }
            }
            if (currency >= amount) {
                var remainingCost = amount
                for (stack in player.inventory.itemStacks) {
                    if (stack.material().key() == resource.material.key()) {
                        val takeAmount = min(remainingCost, stack.amount())
                        player.inventory.takeItemStack(stack.withAmount(takeAmount), TransactionOption.ALL)
                        remainingCost -= takeAmount
                        if (remainingCost == 0) {
                            break
                        }
                    }
                }
                player.inventory.addItemStackFirstOffHand(itemStack)
                player.playSound(Sound.sound(SoundEvent.BLOCK_NOTE_BLOCK_PLING, Sound.Source.MASTER, 1f, 2f))
            } else {
                player.sendTranslatedError("game.shop.too_expensive", amount - currency, resource.title)
                player.playSound(Sound.sound(SoundEvent.BLOCK_DISPENSER_FAIL, Sound.Source.MASTER, 1f, 0.5f))
            }
        }
    }

    companion object {
        fun teamConcrete(game: Game, player: Player): ItemStack {
            val material = if (game.hasModule<TeamModule>()) {
                val blockColor = game.getModule<TeamModule>().playerTeams[player.uuid]?.blockColor ?: "white"
                Material.fromNamespaceId("minecraft:${blockColor}_concrete") ?: Material.WHITE_CONCRETE
            } else {
                Material.WHITE_CONCRETE
            }
            return ItemStack.of(material)
        }
    }
}