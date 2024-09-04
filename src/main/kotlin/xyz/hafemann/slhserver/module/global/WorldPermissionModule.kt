package xyz.hafemann.slhserver.module.global

import net.kyori.adventure.key.Key
import net.minestom.server.coordinate.Point
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.PlayerBlockBreakEvent
import net.minestom.server.event.player.PlayerBlockPlaceEvent
import xyz.hafemann.slhserver.game.Game
import xyz.hafemann.slhserver.module.GameModule

class WorldPermissionModule(
    val allowBreakBlocks: Boolean = false,
    val allowPlaceBlocks: Boolean = false,
    val allowBreakMap: Boolean = false,
    val exceptions: List<Key> = listOf(),
) : GameModule() {

    private val playerPlacedBlocks = mutableListOf<Point>()

    override val eventPriority: Int
        get() = -999 // Lower numbers run first; this module needs to have priority to cancel events early

    override fun initialize(parent: Game, eventNode: EventNode<Event>) {
        eventNode.addListener(PlayerBlockBreakEvent::class.java) { event ->
            if (exceptions.contains(event.block.key())) return@addListener

            event.isCancelled = !allowBreakBlocks

            if (allowBreakBlocks && !allowBreakMap) {
                if (playerPlacedBlocks.contains(event.blockPosition)) {
                    playerPlacedBlocks.remove(event.blockPosition)
                } else {
                    event.isCancelled = true
                }
            }
        }

        eventNode.addListener(PlayerBlockPlaceEvent::class.java) { event ->
            if (exceptions.contains(event.block.key())) return@addListener

            event.isCancelled = !allowPlaceBlocks

            if (!event.instance.getBlock(event.blockPosition).isAir) {
                event.isCancelled = true
            }
            if (!event.isCancelled && allowBreakBlocks && !allowBreakMap) {
                playerPlacedBlocks.add(event.blockPosition)
            }
        }
    }
}