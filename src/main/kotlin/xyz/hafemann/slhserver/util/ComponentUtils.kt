package xyz.hafemann.slhserver.util

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Entity
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player
import net.minestom.server.item.ItemStack

val messageColor: TextColor = BRAND_COLOR_PRIMARY
val fieldColor: TextColor = BRAND_COLOR_SECONDARY
val errorColor: TextColor = ERROR_COLOR_SECONDARY
val errorFieldColor: TextColor = ERROR_COLOR_PRIMARY

fun translateMessage(key: String, vararg fields: Any): Component {
    return formatTranslatedMessage(key, messageColor, fieldColor, *fields)
}

fun translateError(key: String, singleColor: Boolean = false, vararg fields: Any): Component {
    return formatTranslatedMessage(key, errorColor, if (singleColor) errorColor else errorFieldColor, *fields)
}

fun Audience.sendTranslated(key: String, vararg fields: Any) {
    this.sendMessage(translateMessage(key, *fields))
}

fun Audience.sendTranslatedError(key: String, vararg fields: Any) {
    this.sendMessage(translateError(key, false, *fields))
}

fun Component.translate(key: String, vararg fields: ComponentLike): Component {
    return this.append(Component.translatable(key, *fields))
}

fun Component.translate(key: String, color: TextColor): Component {
    return this.append(Component.translatable(key, color))
}

fun Component.translate(key: String, color: TextColor, vararg fields: ComponentLike): Component {
    return this.append(Component.translatable(key, color, *fields))
}

fun Component.translate(key: String, color: TextColor, decoration: TextDecoration): Component {
    return this.append(Component.translatable(key, color, decoration))
}

private fun formatTranslatedMessage(
    key: String,
    messageColor: TextColor,
    fieldColor: TextColor,
    vararg values: Any,
): Component {
    return Component.translatable(key, messageColor, values.map {
        if (it is Iterable<*> && it.all { entity -> entity is Entity }) {
            return@map formatEntityDescriptor(it.map { entity -> entity as Entity }, fieldColor)
        }
        when (it) {
            is Component -> it
            is Pos -> formatPosition(it)
            is Entity -> formatEntityDescriptor(listOf(it), fieldColor)
            is GameMode -> Component.translatable("gameMode.${it.toString().lowercase()}", fieldColor)
            is ItemStack -> Component.translatable(it.material().registry().translationKey(), fieldColor)
                .hoverEvent(it.asHoverEvent())

            else -> Component.text(it.toString(), fieldColor)
        }
    })
}

private fun formatEntityDescriptor(entities: List<Entity>, fieldColor: TextColor): Component {
    if (entities.size == 1) {
        val entity = entities[0]
        return if (entity is Player) {
            entity.name.color(fieldColor)
        } else {
            Component.translatable(entity.entityType.registry().translationKey(), fieldColor)
        }
    } else {
        val onlyPlayers = entities.map { it is Player }.contains(false)
        val descriptorKey = if (onlyPlayers) {
            "command.descriptor.players"
        } else {
            "command.descriptor.entities"
        }
        return Component.translatable(descriptorKey, fieldColor, Component.text(entities.size))
    }
}

private fun formatPosition(pos: Pos): Component {
    return Component.text(String.format("%.2f", pos.x), fieldColor)
        .append(Component.text(", ", messageColor))
        .append(Component.text(String.format("%.2f", pos.y), fieldColor))
        .append(Component.text(", ", messageColor))
        .append(Component.text(String.format("%.2f", pos.z), fieldColor))
}