package xyz.hafemann.slhserver.service

import net.minestom.server.MinecraftServer
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.PlayerBlockPlaceEvent
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockHandler
import net.minestom.server.utils.NamespaceID

class EnderchestHandler : BlockHandler {
    override fun onInteract(interaction: BlockHandler.Interaction): Boolean {
        interaction.player.sendMessage("Interacted")
        return false
    }

    override fun getNamespaceId(): NamespaceID {
        return NamespaceID.from("minecraft:ender_chest")
    }

    class Listener {
        init {
            val playerNode = EventNode.all("enderchest-events")

            playerNode.addListener(PlayerBlockPlaceEvent::class.java, ::onBlockPlace)

            MinecraftServer.getGlobalEventHandler().addChild(playerNode)
        }

        private fun onBlockPlace(event: PlayerBlockPlaceEvent) {
            if (event.block.key().toString().endsWith("_bed")) {
                event.block = event.block.withHandler(EnderchestHandler())
            }
        }
    }
}