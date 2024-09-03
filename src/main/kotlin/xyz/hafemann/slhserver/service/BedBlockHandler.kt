package xyz.hafemann.slhserver.service

import net.minestom.server.MinecraftServer
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.PlayerBlockPlaceEvent
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockHandler
import net.minestom.server.utils.NamespaceID

class BedBlockHandler : BlockHandler {
    override fun onDestroy(destroy: BlockHandler.Destroy) {
        val block = destroy.block
        val pos = destroy.blockPosition

        val part = block.getProperty("part")
        val facing = block.getProperty("facing")

        var x = 0.0
        var z = 0.0
        when (facing) {
            "north" -> z--
            "east" -> x++
            "south" -> z++
            "west" -> x--
        }
        if (part == "head") {
            x *= -1
            z *= -1
        }

        val neighborPos = pos.add(x, 0.0, z)
        val neighbor = destroy.instance.getBlock(neighborPos)
        if (neighbor.key() == block.key() && neighbor.getProperty("part") != part) {
            destroy.instance.setBlock(neighborPos, Block.AIR)
        }
    }

    override fun getNamespaceId(): NamespaceID {
        return NamespaceID.from("minecraft:bed")
    }

    class Listener {
        init {
            val playerNode = EventNode.all("bed-events")

            playerNode.addListener(PlayerBlockPlaceEvent::class.java, ::onBlockPlace)

            MinecraftServer.getGlobalEventHandler().addChild(playerNode)
        }

        private fun onBlockPlace(event: PlayerBlockPlaceEvent) {
            if (event.block.key().toString().endsWith("_bed")) {
                event.block = event.block.withHandler(BedBlockHandler())
            }
        }
    }
}