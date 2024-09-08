package xyz.hafemann.slhserver.module.minigame

import net.kyori.adventure.text.Component
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.scoreboard.Sidebar
import xyz.hafemann.slhserver.event.GameStateChangedEvent
import xyz.hafemann.slhserver.game.Game
import xyz.hafemann.slhserver.game.GameState
import xyz.hafemann.slhserver.module.GameModule

class ScoreboardModule : GameModule() {
    override fun initialize(parent: Game, eventNode: EventNode<Event>) {
        eventNode.addListener(GameStateChangedEvent::class.java) { event ->
            when (event.newState) {
                GameState.RUNNING -> {
                    val sidebar = Sidebar(Component.text("Bed Wars"))
                    for (player in parent.instance.players) {
                        sidebar.addViewer(player)
                    }
                }
                else -> {}
            }
        }
    }
}