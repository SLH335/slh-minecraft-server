package xyz.hafemann.slhserver.module.minigame

import net.minestom.server.entity.EquipmentSlot
import net.minestom.server.entity.Player
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.event.inventory.InventoryPreClickEvent
import net.minestom.server.event.player.PlayerRespawnEvent
import net.minestom.server.item.ItemComponent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.item.component.DyedItemColor
import xyz.hafemann.slhserver.event.GameStateChangedEvent
import xyz.hafemann.slhserver.game.Game
import xyz.hafemann.slhserver.game.GameState
import xyz.hafemann.slhserver.module.DependsOn
import xyz.hafemann.slhserver.module.GameModule
import java.util.UUID

@DependsOn(TeamModule::class)
class EquipmentModule : GameModule() {
    val armor = mutableMapOf<UUID, ArmorType>()

    override fun initialize(parent: Game, eventNode: EventNode<Event>) {
        eventNode.addListener(GameStateChangedEvent::class.java) { event ->
            if (event.newState == GameState.RUNNING) {
                for (player in parent.players) {
                    setEquipment(player, parent)
                }
            }
        }
        eventNode.addListener(PlayerRespawnEvent::class.java) { event ->
            if (parent.state != GameState.RUNNING) return@addListener
            val player = event.player
            if (player !in parent.players) return@addListener

            setEquipment(player, parent)
        }
        // prevent taking off armor and interacting with crafting field
        eventNode.addListener(InventoryPreClickEvent::class.java) { event ->
            if (event.inventory != null) return@addListener
            if (event.slot in 36..44) {
                event.isCancelled = true
            }
        }
    }

    private fun setEquipment(player: Player, game: Game) {
        val team = game.getModule<TeamModule>().getTeam(player)!!

        setArmor(player, team)
        setTools(player)
    }

    private fun setArmor(player: Player, team: Team) {
        val armor = listOf(
            Material.LEATHER_HELMET,
            Material.LEATHER_CHESTPLATE,
            Material.LEATHER_LEGGINGS,
            Material.LEATHER_BOOTS
        ).map {
            ItemStack.builder(it).set(ItemComponent.DYED_COLOR, DyedItemColor(team.armorColor)).build()
        }
        player.inventory.setEquipment(EquipmentSlot.HELMET, armor[0])
        player.inventory.setEquipment(EquipmentSlot.CHESTPLATE, armor[1])
        player.inventory.setEquipment(EquipmentSlot.LEGGINGS, ItemStack.of(Material.IRON_LEGGINGS))
        player.inventory.setEquipment(EquipmentSlot.BOOTS, ItemStack.of(Material.IRON_BOOTS))
    }

    private fun setTools(player: Player) {
        player.inventory.addItemStack(ItemStack.of(Material.WOODEN_SWORD, 1))
    }
}

enum class ArmorType {
    LEATHER,
    CHAINMAIL,
    IRON,
    DIAMOND
}