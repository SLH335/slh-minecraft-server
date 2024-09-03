package xyz.hafemann.slhserver.command

import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity
import net.minestom.server.command.builder.arguments.minecraft.ArgumentItemStack
import net.minestom.server.command.builder.arguments.number.ArgumentInteger
import net.minestom.server.entity.Player
import net.minestom.server.item.ItemStack
import xyz.hafemann.slhserver.util.sendTranslated

class GiveCommand(name: String, vararg aliases: String) : SLHCommand(name, aliases, block = {
    usage("<player> <item> [count]")

    val playersArg = ArgumentEntity("players").onlyPlayers(true)
    val itemArg = ArgumentItemStack("item")
    val countArg = ArgumentInteger("count")

    fun execute(sender: CommandSender, players: List<Player>, item: ItemStack) {
        for (player in players) {
            player.inventory.addItemStack(item)
            if (player != sender) {
                player.sendTranslated("command.give.self", item.amount(), item)
            }
        }

        sender.sendTranslated("command.give.other", item.amount(), item, players)
    }

    syntax(playersArg, itemArg) {
        val players = getPlayers(playersArg)
        val item = get(itemArg)

        execute(sender, players, item)
    }

    syntax(playersArg, itemArg, countArg) {
        val players = getPlayers(playersArg)
        val item = get(itemArg)
        val count = get(countArg)

        execute(sender, players, item.withAmount(count))
    }
})