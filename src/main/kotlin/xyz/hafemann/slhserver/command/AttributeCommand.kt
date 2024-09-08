package xyz.hafemann.slhserver.command

import net.kyori.adventure.text.Component
import net.minestom.server.command.builder.arguments.ArgumentWord
import net.minestom.server.command.builder.arguments.number.ArgumentDouble
import net.minestom.server.command.builder.suggestion.SuggestionEntry
import net.minestom.server.entity.attribute.Attribute
import net.minestom.server.entity.attribute.AttributeModifier
import net.minestom.server.entity.attribute.AttributeOperation
import xyz.hafemann.slhserver.util.fieldColor
import xyz.hafemann.slhserver.util.sendTranslated
import xyz.hafemann.slhserver.util.sendTranslatedError

class AttributeCommand(name: String, vararg aliases: String) : SLHCommand(name, aliases, block = {
    usage("<attribute> <value>")

    val attributeArg = ArgumentWord("attribute").setSuggestionCallback { sender, context, suggestion ->
        val input: String = context.get("attribute")

        val attributes = Attribute.values().map { it.toString().replace("minecraft:", "") }
            .filter { it.startsWith("generic") || it.startsWith("player") }

        for (attribute in attributes) {
            if (input.toCharArray()[0].code == 0 || attribute.contains(input)) {
                suggestion.addEntry(SuggestionEntry(attribute, Component.empty()))
            }
        }
    }
    val factorArg = ArgumentDouble("factor")

    syntax(attributeArg) {
        sender.sendTranslatedError("command.error.usage", context.commandName, "<attribute> <value>")
    }

    syntax(attributeArg, factorArg) {
        val attributeStr = get(attributeArg)
        val factor = get(factorArg)

        val attribute = Attribute.fromNamespaceId(attributeStr) ?: return@syntax
        player.getAttribute(attribute)
            .addModifier(AttributeModifier(attributeStr, factor-1, AttributeOperation.MULTIPLY_BASE))

        sender.sendTranslated(
                "command.attribute.self",
                Component.translatable(attribute.registry().translationKey, fieldColor),
                factor
            )
    }.requirePlayer()
})