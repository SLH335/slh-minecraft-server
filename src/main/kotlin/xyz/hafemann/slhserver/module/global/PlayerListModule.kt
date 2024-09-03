package xyz.hafemann.slhserver.module.global

import net.kyori.adventure.text.Component
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.PlayerSpawnEvent
import xyz.hafemann.slhserver.game.Game
import xyz.hafemann.slhserver.module.GameModule
import xyz.hafemann.slhserver.util.BRAND_COLOR_PRIMARY
import xyz.hafemann.slhserver.util.BRAND_COLOR_SECONDARY

class PlayerListModule(
    private val header: Component = Component.translatable("global.server.name", BRAND_COLOR_PRIMARY).appendNewline(),
    private val footer: Component = Component.newline()
        .append(Component.translatable("global.server.domain", BRAND_COLOR_SECONDARY))
) : GameModule() {
    override fun initialize(parent: Game, eventNode: EventNode<Event>) {
        eventNode.addListener(PlayerSpawnEvent::class.java) { event ->
            event.player.sendPlayerListHeaderAndFooter(header, footer)
        }
    }
}