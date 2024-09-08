package xyz.hafemann.slhserver.module.minigame

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.event.entity.EntityDamageEvent
import xyz.hafemann.slhserver.event.GameStateChangedEvent
import xyz.hafemann.slhserver.game
import xyz.hafemann.slhserver.game.Game
import xyz.hafemann.slhserver.game.GameState
import xyz.hafemann.slhserver.game.TeamProperties
import xyz.hafemann.slhserver.module.GameModule
import xyz.hafemann.slhserver.util.messageColor
import java.util.UUID

class TeamModule(
    val teamSize: Int,
    val teamCount: Int,
) : GameModule() {

    val teams = mutableListOf<Team>()
    val playerTeams = mutableMapOf<UUID, Team>()

    override fun initialize(parent: Game, eventNode: EventNode<Event>) {
        for (teamId in 0..<teamCount) {
            val team = Team.fromId(teamId)
            teams.add(team)
        }

        // Assign players to teams when game starts
        eventNode.addListener(GameStateChangedEvent::class.java) { event ->
            if (event.newState == GameState.RUNNING) {
                for (player in parent.players) {
                    val team = assignTeam(player)
                    player.teleport(team.respawnPoint())
                }
            }
        }

        // Prevent friendly fire
        eventNode.addListener(EntityDamageEvent::class.java) { event ->
            val player = event.entity
            val attacker = event.damage.attacker
            if (player !is Player) return@addListener
            if (attacker !is Player) return@addListener
            if (playerTeams[attacker.uuid] == playerTeams[player.uuid]) {
                event.isCancelled = true
            }
        }
    }

    fun getTeam(player: Player): Team? {
        return playerTeams[player.uuid]
    }

    fun Player.coloredName(): Component {
        val color = getTeam(this)?.color ?: NamedTextColor.WHITE
        return Component.text(this.username, color)
    }

    private fun assignTeam(player: Player): Team {
        val team = chooseTeam()
        addToTeam(player, team)
        return team
    }

    private fun addToTeam(player: Player, team: Team) {
        playerTeams[player.uuid] = team
        team.join(player)
        player.respawnPoint = team.respawnPoint()
        //player.displayName = Component.empty().append(Component.text(team.name[0], team.color).decorate(TextDecoration.BOLD)).appendSpace()
        //    .append(Component.text(player.username, team.color).decoration(TextDecoration.BOLD, false))
        player.displayName = Component.text(player.username, team.color)
        player.sendMessage(Component.text("You have been assigned to Team ", messageColor).append(team.title))
    }

    private fun chooseTeam(): Team {
        val minTeamSize = teams.minBy { it.members.size }.members.size
        val randomTeam = teams.filter { it.members.size == minTeamSize }.random()
        if (teamSize < 0 || randomTeam.members.size < teamSize) {
            return randomTeam
        }
        error("Failed to choose team, all teams are already full")
    }
}

enum class Team(val id: Int, val key: String, val color: NamedTextColor, val blockColor: String, val armorColor: Int) {
    RED(6, "red", NamedTextColor.RED, "red", 0xB02E26),
    BLUE(2, "blue", NamedTextColor.BLUE, "blue", 0x3C44AA),
    GREEN(1, "green", NamedTextColor.GREEN, "lime", 0x80C71F),
    YELLOW(3, "yellow", NamedTextColor.YELLOW, "yellow", 0xFED83D),
    AQUA(4, "aqua", NamedTextColor.AQUA, "cyan", 0x169C9C),
    WHITE(5, "white", NamedTextColor.WHITE, "white", 0xF9FFFE),
    PINK(0, "pink", NamedTextColor.LIGHT_PURPLE, "pink", 0xF38BAA),
    GRAY(7, "gray", NamedTextColor.DARK_GRAY, "gray", 0x474F52);

    val members = mutableListOf<Player>()

    val title get() = Component.translatable("team.$key", color)

    fun join(player: Player) {
        members.add(player)
    }

    fun getProperties(): TeamProperties {
        return game.mapProperties.teams.find { it.id == this.key }!!
    }

    fun respawnPoint(): Pos {
        return getProperties().pos
    }

    companion object {
        fun fromId(id: Int): Team {
            return entries.first { it.id == id }
        }
    }
}