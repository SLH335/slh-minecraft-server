package xyz.hafemann.slhserver.service.nametag

import net.kyori.adventure.text.Component
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.LivingEntity
import net.minestom.server.entity.attribute.Attribute
import net.minestom.server.tag.Tag
import java.util.*

class Nametag(name: Component, val id: UUID = UUID.randomUUID()) : LivingEntity(EntityType.ARMOR_STAND) {
    init {
        this.isCustomNameVisible = true
        this.isInvisible = true
        this.getAttribute(Attribute.GENERIC_SCALE).baseValue = 0.0
        this.customName = name
        this.setTag(Tag.UUID("nametag-id"), id)
        this.updateViewableRule { player -> player.uuid != id }
    }
}