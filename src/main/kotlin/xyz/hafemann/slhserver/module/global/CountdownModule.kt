package xyz.hafemann.slhserver.module.global

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.Title.Times
import net.minestom.server.MinecraftServer
import net.minestom.server.adventure.audience.Audiences
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.timer.TaskSchedule
import xyz.hafemann.slhserver.event.GameStateChangedEvent
import xyz.hafemann.slhserver.game.Game
import xyz.hafemann.slhserver.game.GameState
import xyz.hafemann.slhserver.module.GameModule
import xyz.hafemann.slhserver.util.messageColor
import xyz.hafemann.slhserver.util.sendTranslated
import java.time.Duration

class CountdownModule(val seconds: Int = 1) : GameModule() {
    private val scheduler = MinecraftServer.getSchedulerManager()

    override fun initialize(parent: Game, eventNode: EventNode<Event>) {
        eventNode.addListener(GameStateChangedEvent::class.java) { event ->
            if (event.newState != GameState.STARTING) return@addListener

            var countdown = seconds
            scheduler.submitTask {
                when (countdown) {
                    in 1..5 -> {
                        Audiences.server().sendTranslated("game.start.countdown", Component.text(countdown, NamedTextColor.RED))
                        Audiences.players().showTitle(
                            Title.title(
                                Component.text(countdown, NamedTextColor.RED),
                                Component.empty(),
                                Times.times(Duration.ZERO, Duration.ofSeconds(1), Duration.ZERO),
                            )
                        )
                    }
                    0 -> {
                        parent.state = GameState.RUNNING
                        return@submitTask TaskSchedule.stop()
                    }
                }
                countdown--
                return@submitTask TaskSchedule.seconds(1)
            }
        }
    }
}