package xyz.hafemann.slhserver.service.npc

import net.kyori.adventure.text.Component
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.*
import net.minestom.server.entity.metadata.PlayerMeta
import net.minestom.server.event.EventNode
import net.minestom.server.event.entity.EntitySpawnEvent
import net.minestom.server.event.player.PlayerEntityInteractEvent
import net.minestom.server.network.packet.server.play.PlayerInfoRemovePacket
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket
import xyz.hafemann.slhserver.service.nametag.Nametag

@Suppress("UnstableApiUsage")
class NPC(
    val name: Component,
    val skin: PlayerSkin? = null,
    val type: EntityType = EntityType.PLAYER,
    val interaction: (Player, NPC) -> Unit,
) : LivingEntity(type) {

    init {
        registerListeners()

        if (type == EntityType.PLAYER) {
            enableSkinLayers()
        }
    }

    private fun registerListeners() {
        val eventNode = EventNode.all("npc-events")
        eventNode.addListener(PlayerEntityInteractEvent::class.java) { event ->
            if (event.hand != Player.Hand.MAIN) return@addListener
            if (event.target != this) return@addListener
            interaction.invoke(event.player, event.target as NPC)
        }
        eventNode.addListener(EntitySpawnEvent::class.java) { event ->
            if (event.entity != this) return@addListener

            val nametag = Nametag(name)
            nametag.instance = instance
            this.addPassenger(nametag)
        }
        MinecraftServer.getGlobalEventHandler().addChild(eventNode)
    }

    override fun updateNewViewer(player: Player) {
        if (type == EntityType.PLAYER) {
            val meta = this.entityMeta as PlayerMeta

            val textureProperty = PlayerInfoUpdatePacket.Property("textures", skin?.textures() ?: "", skin?.signature())
            val playerEntry = PlayerInfoUpdatePacket.Entry(
                this.uuid,
                "",
                listOf(textureProperty),
                false,
                0,
                GameMode.CREATIVE,
                meta.customName,
                null
            )

            val addPlayerPacket = PlayerInfoUpdatePacket(
                PlayerInfoUpdatePacket.Action.ADD_PLAYER,
                playerEntry
            )

            player.sendPacket(addPlayerPacket)

        }
        super.updateNewViewer(player)
    }

    override fun updateOldViewer(player: Player) {
        super.updateOldViewer(player)
        if (type == EntityType.PLAYER) {
            player.sendPacket(PlayerInfoRemovePacket(uuid))
        }
    }

    private fun enableSkinLayers() {
        val meta = this.entityMeta as PlayerMeta
        meta.setNotifyAboutChanges(false)
        meta.isCapeEnabled = true
        meta.isHatEnabled = true
        meta.isJacketEnabled = true
        meta.isLeftLegEnabled = true
        meta.isLeftSleeveEnabled = true
        meta.isRightLegEnabled = true
        meta.isRightSleeveEnabled = true
        meta.setNotifyAboutChanges(true)
    }

    companion object {
        fun buildInteraction(interactionString: String): (Player, NPC) -> Unit {
            val split = interactionString.split('-')
            when (split[0]) {
                "game" -> {
                    when (split[1]) {
                        "bedwars" -> {
                            when (split[2]) {
                                "play" -> return { player, npc ->
                                    player.sendMessage("Opening Bedwars Menu")
                                }
                            }
                        }
                    }
                }
            }
            return { player, npc -> player.sendMessage("Clicked NPC") }
        }
    }
}