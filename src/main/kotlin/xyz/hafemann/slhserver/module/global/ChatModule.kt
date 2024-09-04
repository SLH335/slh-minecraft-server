package xyz.hafemann.slhserver.module.global

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.PlayerChatEvent
import xyz.hafemann.slhserver.game.Game
import xyz.hafemann.slhserver.module.GameModule

class ChatModule : GameModule() {
    override fun initialize(parent: Game, eventNode: EventNode<Event>) {
        eventNode.addListener(PlayerChatEvent::class.java) { event ->
            event.setChatFormat { e ->
                e.player.name
                    .append(Component.text(" > ", NamedTextColor.GRAY))
                    .append(Component.text(e.message))
            }
        }
    }
}