package xyz.hafemann.slhserver.module.global

import net.kyori.adventure.text.Component
import net.minestom.server.entity.Player
import net.minestom.server.entity.PlayerSkin
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.event.entity.EntityDamageEvent
import xyz.hafemann.slhserver.game.Game
import xyz.hafemann.slhserver.module.GameModule
import xyz.hafemann.slhserver.module.SoftDependsOn
import xyz.hafemann.slhserver.service.npc.NPC

@SoftDependsOn(GUIModule::class)
class NPCModule : GameModule() {
    override fun initialize(parent: Game, eventNode: EventNode<Event>) {
        for (npc in parent.mapProperties.npcs) {
            val entity = NPC(
                Component.text(npc.name),
                PlayerSkin(npc.skin?.textures, npc.skin?.signature),
                npc.type,
                npcInteraction(parent, npc.action),
            )
            entity.setInstance(parent.instance, npc.pos)
            logger.debug("Spawned NPC ${npc.name}")
        }

        eventNode.addListener(EntityDamageEvent::class.java) { event ->
            if (event.entity is NPC) event.isCancelled = true
        }
    }

    private fun npcInteraction(game: Game, interactionString: String): (Player, NPC) -> Unit {
        val split = interactionString.split('.')
        when (split[0]) {
            "game" -> {
                when (split[1]) {
                    "bedwars" -> {
                        when (split[2]) {
                            "play" -> return { player, _ ->
                                player.sendMessage("Opening Bedwars Menu")
                            }
                            "shop" -> return { player, _ ->
                                if (game.hasModule<GUIModule>()) {
                                    player.openInventory(game.getModule<GUIModule>().createShop(game, player))
                                }
                            }
                        }
                    }
                }
            }
        }
        return { player, npc -> player.sendMessage("Clicked NPC") }
    }
}