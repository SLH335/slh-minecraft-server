package xyz.hafemann.slhserver.util

import net.minestom.server.inventory.PlayerInventory
import net.minestom.server.item.ItemComponent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.component.EnchantmentList
import net.minestom.server.item.enchant.Enchantment
import net.minestom.server.registry.DynamicRegistry
import kotlin.math.min

// add item stack to player inventory while first filling up offhand slot if it contains the same item
fun PlayerInventory.addItemStackFirstOffHand(itemStack: ItemStack) {
    var remainingCount = itemStack.amount()
    if (this.itemInOffHand.isSimilar(itemStack)) {
        val newOffHandAmount = min(this.itemInOffHand.amount() + remainingCount, itemStack.maxStackSize())
        remainingCount -= newOffHandAmount - this.itemInOffHand.amount()
        this.itemInOffHand = itemStack.withAmount(newOffHandAmount)
    }
    this.addItemStack(itemStack.withAmount(remainingCount))
}

fun ItemStack.withEnchantment(enchantment: DynamicRegistry.Key<Enchantment>, level: Int): ItemStack {
    return this.with(ItemComponent.ENCHANTMENTS, EnchantmentList(mutableMapOf(enchantment to level)))
}