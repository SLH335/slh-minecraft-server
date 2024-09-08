package xyz.hafemann.slhserver.module.config

import com.charleskorn.kaml.Yaml
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import xyz.hafemann.slhserver.game.Game
import xyz.hafemann.slhserver.game.GameMapProperties
import xyz.hafemann.slhserver.game.GameProperties
import xyz.hafemann.slhserver.module.GameModule
import java.nio.file.Paths

class GameConfigModule : GameModule() {

    override fun initialize(parent: Game, eventNode: EventNode<Event>) {
        val gamePath = Paths.get("games/${parent.name}.yml")
        val mapPath = Paths.get("worlds/${parent.name}/${parent.mode}/${parent.map}/map-config.yml")

        parent.properties = Yaml.default.decodeFromString(GameProperties.serializer(), gamePath.toFile().readText())

        parent.modeProperties = parent.properties.modes.find { it.id == parent.mode }
            ?: error("Properties for ${parent.name} mode ${parent.mode} could not be loaded")

        parent.mapProperties = Yaml.default.decodeFromString(GameMapProperties.serializer(), mapPath.toFile().readText())
    }
}