package xyz.hafemann.slhserver.module.global

import io.github.togar2.pvp.feature.CombatFeatures
import io.github.togar2.pvp.feature.FeatureType
import io.github.togar2.pvp.feature.provider.DifficultyProvider
import io.github.togar2.pvp.utils.CombatVersion
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import xyz.hafemann.slhserver.game.Game
import xyz.hafemann.slhserver.module.GameModule

class PVPModule : GameModule() {
    override fun initialize(parent: Game, eventNode: EventNode<Event>) {
        val modernVanilla = CombatFeatures.getVanilla(CombatVersion.MODERN, DifficultyProvider.DEFAULT)
            .remove(FeatureType.ITEM_DAMAGE).build()
        eventNode.addChild(modernVanilla.createNode())
    }
}