package xyz.hafemann.slhserver.event

import net.minestom.server.event.Event
import xyz.hafemann.slhserver.game.Game

abstract class GameEvent(val game: Game) : Event