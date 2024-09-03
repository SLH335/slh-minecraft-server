package xyz.hafemann.slhserver.command

import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.Argument
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeVec2
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeVec3
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player
import net.minestom.server.utils.entity.EntityFinder
import xyz.hafemann.slhserver.util.sendTranslatedError

open class SLHCommand(
    name: String,
    aliases: Array<out String?> = emptyArray(),
    val permission: String = "command.$name",
    block: SLHCommand.() -> Unit,
) : Command(name, *aliases), ConditionHolder {
    override val conditions: MutableList<ConditionCtx.() -> Boolean> = mutableListOf()

    private fun requirePermission() {
        setCondition { sender, cmd ->
            if (!sender.hasPermission(permission)) {
                if (cmd != null) {
                    sender.sendTranslatedError("command.error.unknown", cmd.split(" ")[0])
                }
                false
            } else true
        }
    }

    init {
        requirePermission()
        block()
    }

    fun usage(usageString: String = "") {
        setDefaultExecutor { sender, context ->
            sender.sendTranslatedError("command.error.usage", context.commandName, usageString)
        }
    }

    fun subcommand(name: String, block: SLHCommand.() -> Unit) {
        addSubcommand(constructSubcommand(name, block))
    }

    private fun constructSubcommand(name: String, block: SLHCommand.() -> Unit): Command {
        return SLHCommand(name, emptyArray(), permission, block)
    }

    fun syntax(vararg args: Argument<*>, block: CommandCtx.() -> Unit) = Syntax(this, args.toList(), block)

    open class CommandCtx(val sender: CommandSender, val context: CommandContext) {
        val player by lazy { sender as Player }

        fun <T> get(argument: Argument<T>): T = context.get(argument)

        fun getPlayers(argument: Argument<EntityFinder>): List<Player> {
            val players = context.get(argument).find(sender).filterIsInstance<Player>()
            if (players.isEmpty()) {
                sender.sendTranslatedError("command.error.no_player_found")
                throw SilentCommandException("No player found")
            }
            return players
        }

        fun getFirstPlayer(argument: Argument<EntityFinder>): Player {
            return context.get(argument).findFirstPlayer(sender) ?: run {
                sender.sendTranslatedError("command.error.no_player_found")
                throw SilentCommandException("No player found")
            }
        }

        fun getPos(locationArgument: ArgumentRelativeVec3, id: String = "location"): Pos {
            val senderPos = if (sender is Player) sender.position else Pos.ZERO
            val split = context.getRaw(id).split(" ")
            val coordinates = mutableListOf(0.0, 0.0, 0.0)
            val isRelative = mutableListOf(false, false, false)
            val isInt = mutableListOf(false, false, false)

            for ((i, coord) in split.withIndex()) {
                coordinates[i] = coord.replace("~", "").replace("^", "").toDoubleOrNull() ?: 0.0
                isRelative[i] = coord.startsWith('~')
                isInt[i] = !isRelative[i] && !coord.contains('.')
            }

            val pos = if (split[0].startsWith('^')) {
                context.get(locationArgument).fromSender(sender).asPosition()
            } else {
                Pos(
                    coordinates[0] + if (isRelative[0]) senderPos.x else if (isInt[0]) 0.5 else 0.0,
                    coordinates[1] + if (isRelative[1]) senderPos.y else 0.0,
                    coordinates[2] + if (isRelative[2]) senderPos.z else if (isInt[2]) 0.5 else 0.0,
                    senderPos.yaw,
                    senderPos.pitch,
                )
            }

            return pos
        }

        fun getPos(
            locationArgument: ArgumentRelativeVec3,
            rotationArgument: ArgumentRelativeVec2,
            locationId: String = "location",
            rotationId: String = "rotation",
        ): Pos {
            val pos = getPos(locationArgument, locationId)

            val rotation = context.get(rotationArgument).from(Pos.ZERO)
            val split = context.getRaw(rotationId).split(" ")
            val yaw = rotation.x.toFloat() + if (split[0].startsWith('~')) player.position.yaw else 0f
            val pitch = rotation.z.toFloat() + if (split[1].startsWith('~')) player.position.pitch else 0f

            return pos.withView(yaw, pitch)
        }

        fun ifNotImmune(target: Player, permission: String = "bypass.command", block: CommandCtx.() -> Unit): Boolean {
            if (target != sender && target.hasPermission(permission)) {
                sender.sendTranslatedError("command.error.bypass", target)
                target.sendTranslatedError("command.bypass.log", sender, context.input)
                return false
            } else {
                block.run {  }
                return true
            }
        }

        fun forEachIfNotImmune(
            targets: List<Player>,
            permission: String = "bypass.command",
            block: (Player) -> Unit
        ): List<Player> {
            val affectedTargets = mutableListOf<Player>()
            for (target in targets) {
                if (target != sender && target.hasPermission(permission)) {
                    sender.sendTranslatedError("command.error.bypass", target)
                    target.sendTranslatedError("command.bypass.log", sender, context.input)
                } else {
                    block.invoke(target)
                    affectedTargets.add(target)
                }
            }
            return affectedTargets
        }
    }

    data class ConditionCtx(val sender: CommandSender, val context: CommandContext)

    open class Syntax(parent: Command, args: List<Argument<*>>, handler: CommandCtx.() -> Unit) : ConditionHolder {
        override val conditions: MutableList<ConditionCtx.() -> Boolean> = mutableListOf()

        init {
            val permission = (parent as SLHCommand).permission
            this.requirePermission(permission)

            parent.addSyntax({ sender, context ->
                try {
                    if (!conditionsPass(ConditionCtx(sender, context))) return@addSyntax
                    handler(CommandCtx(sender, context))
                } catch (e: Throwable) {
                    if (e is SilentCommandException) return@addSyntax
                    e.printStackTrace()
                    sender.sendTranslatedError("command.error.internal")
                }
            }, *args.toTypedArray())
        }
    }

    class SilentCommandException(message: String?) : RuntimeException(message)
}

interface ConditionHolder {
    val conditions: MutableList<SLHCommand.ConditionCtx.() -> Boolean>

    fun conditionsPass(context: SLHCommand.ConditionCtx) = conditions.all { it(context) }

    fun requirePlayer() {
        conditions.add {
            if (sender !is Player) {
                sender.sendTranslatedError("command.error.only_player")
                false
            } else true
        }
    }

    fun requirePermission(permission: String) {
        conditions.add {
            if (!sender.hasPermission(permission)) {
                sender.sendTranslatedError("command.error.unknown", context.commandName)
                false
            } else true
        }
    }
}