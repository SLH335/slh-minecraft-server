package xyz.hafemann.slhserver.module.minigame

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.event.item.PickupItemEvent
import net.minestom.server.item.Material
import net.minestom.server.timer.Task
import net.minestom.server.timer.TaskSchedule
import xyz.hafemann.slhserver.event.GameStateChangedEvent
import xyz.hafemann.slhserver.game.Game
import xyz.hafemann.slhserver.game.GameState
import xyz.hafemann.slhserver.module.GameModule

class GeneratorModule : GameModule() {
    override fun initialize(parent: Game, eventNode: EventNode<Event>) {
        val generators = parent.mapProperties.generators
        val scheduler = MinecraftServer.getSchedulerManager()

        var counter = 1
        var task: Task? = null

        eventNode.addListener(GameStateChangedEvent::class.java) { event ->
            when (event.newState) {
                GameState.RUNNING -> {
                    task = scheduler.submitTask {
                        for (resourceType in ResourceType.entries) {
                            for (generator in generators.filter { it.resource == resourceType }) {
                                if ((counter % resourceType.spawnFrequency) == 0) {
                                    generator.dropResource(parent.instance)
                                }
                            }
                        }

                        counter++
                        return@submitTask TaskSchedule.seconds(1)
                    }
                }
                GameState.ENDED -> task?.cancel()
                else -> {}
            }
        }

        eventNode.addListener(PickupItemEvent::class.java) { event ->
            if (event.entity !is Player) return@addListener
            val player = event.entity as Player
            for (generator in generators.filter { it.pos.distance(player.position) < 1.5}) {
                if (generator.resource == ResourceType.COPPER || generator.resource == ResourceType.IRON) {
                    val nearPlayers = parent.players.filter { it.getDistance(player) < 1.5 && it.uuid != player.uuid }
                    nearPlayers.forEach { it.inventory.addItemStack(event.itemStack) }
                }
            }
        }
    }
}

enum class ResourceType(val material: Material, val spawnFrequency: Int, val spawnLimit: Int, private val _color: Int) {
    COPPER(Material.COPPER_INGOT, 1, 64, 0xF99780),
    IRON(Material.IRON_INGOT, 8, 16, 0xD5D5D5),
    DIAMOND(Material.DIAMOND, 20, 4, 0x49EAD6),
    GOLD(Material.GOLD_INGOT, 60, 2, 0xF4C230);

    val color get() = TextColor.color(_color)
    val title get() = Component.translatable("material.${name.lowercase()}", color)

    companion object {
        fun fromMaterial(fromMaterial: Material): ResourceType? {
            for (resourceType in entries) {
                if (fromMaterial.key() == resourceType.material.key()) {
                    return resourceType
                }
            }
            return null
        }
    }
}
