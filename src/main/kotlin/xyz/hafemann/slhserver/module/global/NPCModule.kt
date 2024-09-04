package xyz.hafemann.slhserver.module.global

import net.kyori.adventure.text.Component
import net.minestom.server.entity.PlayerSkin
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import xyz.hafemann.slhserver.game.Game
import xyz.hafemann.slhserver.module.GameModule
import xyz.hafemann.slhserver.service.npc.NPC

class NPCModule : GameModule() {
    override fun initialize(parent: Game, eventNode: EventNode<Event>) {
        for (npc in parent.mapProperties.npcs) {
            val entity = NPC(
                Component.text(npc.name),
                PlayerSkin(npc.skin?.textures, npc.skin?.signature),
                npc.type,
                NPC.buildInteraction(npc.action),
            )
            entity.setInstance(parent.instance, npc.pos)
            logger.debug("Spawned NPC ${npc.name}")
        }
    }
}