package xyz.hafemann.slhserver.util

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Entity
import net.minestom.server.entity.Player
import net.minestom.server.item.ItemStack

val messageColor: TextColor = BRAND_COLOR_PRIMARY_2
val fieldColor: TextColor = BRAND_COLOR_PRIMARY_1
val errorColor: TextColor = NamedTextColor.RED
val errorFieldColor: TextColor = NamedTextColor.RED

fun translateMessage(key: String, vararg fields: Any): Component {
    return formatTranslatedMessage(key, messageColor, fieldColor, *fields)
}

fun translateError(key: String, vararg fields: Any): Component {
    return formatTranslatedMessage(key, errorColor, errorFieldColor, *fields)
}

fun Audience.sendTranslated(key: String, vararg fields: Any) {
    this.sendMessage(translateMessage(key, fields))
}

fun Audience.sendTranslatedError(key: String, vararg fields: Any) {
    this.sendMessage(translateError(key, fields))
}

private fun formatTranslatedMessage(
    key: String,
    messageColor: TextColor,
    fieldColor: TextColor,
    vararg values: Any,
): Component {
    return Component.translatable(key, messageColor, values.map {
        if (it is Iterable<*> && it.all { entity -> entity is Entity }) {
            return formatEntityDescriptor(it.map { entity -> entity as Entity })
        }
        when (it) {
            is Component -> it
            is Pos -> formatPosition(it)
            is Entity -> formatEntityDescriptor(listOf(it))
            is ItemStack -> Component.translatable(it.material().registry().translationKey(), fieldColor)
                .hoverEvent(it.asHoverEvent())

            else -> Component.text(it.toString(), fieldColor)
        }
    })
}

private fun formatEntityDescriptor(entities: List<Entity>): Component {
    if (entities.size == 1) {
        val entity = entities[0]
        return if (entity is Player) {
            entity.name
        } else {
            Component.translatable(entity.entityType.registry().translationKey(), fieldColor)
        }
    } else {
        val onlyPlayers = entities.map { it is Player }.contains(false)
        val size = Component.text(entities.size, fieldColor)
        val descriptorKey = if (onlyPlayers) {
            "command.descriptor.players"
        } else {
            "command.descriptor.entities"
        }
        return size.appendSpace().append(Component.translatable(descriptorKey, messageColor))
    }
}

private fun formatPosition(pos: Pos): Component {
    return Component.text(String.format("%.2f", pos.x), fieldColor)
        .append(Component.text(", ", messageColor))
        .append(Component.text(String.format("%.2f", pos.y), fieldColor))
        .append(Component.text(", ", messageColor))
        .append(Component.text(String.format("%.2f", pos.z), fieldColor))
}