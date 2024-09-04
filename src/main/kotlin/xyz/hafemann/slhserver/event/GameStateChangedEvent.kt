package xyz.hafemann.slhserver.event

import xyz.hafemann.slhserver.game.Game
import xyz.hafemann.slhserver.game.GameState

class GameStateChangedEvent(game: Game, val oldState: GameState, val newState: GameState) : GameEvent(game)