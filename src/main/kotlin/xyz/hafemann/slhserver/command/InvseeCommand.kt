package xyz.hafemann.slhserver.command

import net.kyori.adventure.text.Component
import net.minestom.server.MinecraftServer
import net.minestom.server.adventure.audience.Audiences
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity
import net.minestom.server.entity.Player
import net.minestom.server.event.EventFilter
import net.minestom.server.event.EventNode
import net.minestom.server.event.inventory.InventoryClickEvent
import net.minestom.server.event.inventory.InventoryItemChangeEvent
import net.minestom.server.event.inventory.PlayerInventoryItemChangeEvent
import net.minestom.server.inventory.Inventory
import net.minestom.server.inventory.InventoryType

class InvseeCommand(name: String, vararg aliases: String) : SLHCommand(name, aliases, block = {
    usage("<player>")

    val targetArg = ArgumentEntity("player").onlyPlayers(true).singleEntity(true)

    syntax(targetArg) {
        val target = getFirstPlayer(targetArg)

        viewInventory(player, target)

        val eventNode = EventNode.type("invsee-events", EventFilter.INVENTORY)

        eventNode.addListener(PlayerInventoryItemChangeEvent::class.java) { event ->
            if (event.player != target) return@addListener

            if (target.inventory == event.inventory) {
                Audiences.players().sendMessage(Component.text("Is invsee"))
                player.closeInventory()
            }
        }

        MinecraftServer.getGlobalEventHandler().addChild(eventNode)
    }.requirePlayer()
})

private fun viewInventory(sender: Player, target: Player) {
    val targetInventory = target.inventory
    val inventoryView = Inventory(InventoryType.CHEST_6_ROW, "${target.username}'s Inventory")
    target.inventory.cursorItem

    for (i in 0..45) {
        inventoryView.setItemStack(i, targetInventory.getItemStack(i))
    }
    inventoryView.setItemStack(46, targetInventory.cursorItem)

    sender.openInventory(inventoryView)


}