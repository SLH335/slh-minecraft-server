package xyz.hafemann.slhserver.game.bedwars

import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.Title.*
import net.minestom.server.MinecraftServer
import net.minestom.server.adventure.audience.Audiences
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.coordinate.Point
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerBlockBreakEvent
import net.minestom.server.event.player.PlayerDeathEvent
import net.minestom.server.event.player.PlayerRespawnEvent
import net.minestom.server.event.player.PlayerSpawnEvent
import net.minestom.server.instance.block.Block
import net.minestom.server.sound.SoundEvent
import net.minestom.server.timer.TaskSchedule
import xyz.hafemann.slhserver.event.GameStateChangedEvent
import xyz.hafemann.slhserver.game.Game
import xyz.hafemann.slhserver.game.GameState
import xyz.hafemann.slhserver.module.global.*
import xyz.hafemann.slhserver.module.minigame.*
import xyz.hafemann.slhserver.module.vanilla.EnderchestModule
import xyz.hafemann.slhserver.module.vanilla.ItemDropModule
import xyz.hafemann.slhserver.module.vanilla.TNTModule
import xyz.hafemann.slhserver.util.addItemStackFirstOffHand
import xyz.hafemann.slhserver.util.messageColor
import xyz.hafemann.slhserver.util.translate
import java.time.Duration

class Bedwars(mode: String, map: String) : Game("bedwars", mode, map) {
    val activeBeds = mutableMapOf<Team, Boolean>()
    val alivePlayers = mutableListOf<Player>()

    fun canRespawn(player: Player): Boolean {
        val team = getModule<TeamModule>().playerTeams[player.uuid]
        return activeBeds[team] == true
    }

    override fun initialize() {
        use(ChatModule())
        use(PlayerListModule())
        use(WorldPermissionModule(
            allowBreakBlocks = true,
            allowPlaceBlocks = true,
            allowBreakMap = false,
            exceptions = Block.values().filter { it.key().toString().endsWith("_bed") }.map { it.key() }
        ))
        use(SpectatorModule())
        use(ItemDropModule(
            exceptions = Block.values().filter { it.key().toString().endsWith("_bed") }.map { it.key() }
        ))
        use(TeamModule(teamSize = modeProperties.teamSize, teamCount = modeProperties.teamCount))
        use(GUIModule())
        use(NPCModule())
        use(PvPModule())
        use(MapBorderModule(radius = mapProperties.radius))
        use(CountdownModule())
        use(GeneratorModule())
        use(EnderchestModule())
        use(EquipmentModule())
        use(RespawnInvincibilityModule())
        use(TNTModule())
        use(ScoreboardModule())

        getModule<TeamModule>().teams.forEach { activeBeds[it] = true }

        eventNode.addListener(PlayerBlockBreakEvent::class.java) { event ->
            if (!event.block.key().toString().endsWith("_bed")) return@addListener
            val blockColor = event.block.key().toString().split(":")[1].replace("_bed", "")
            for (team in getModule<TeamModule>().teams) {
                if (team.blockColor == blockColor) {
                    if (getModule<TeamModule>().playerTeams[event.player.uuid] == team) {
                        event.isCancelled = true
                        return@addListener
                    }
                    Audiences.server()
                        .sendMessage(
                            Component.newline().translate(
                                "game.bedwars.bed_destroyed",
                                Component.translatable("game.bedwars.bed", team.title).color(team.color),
                                event.player.name,
                            ).appendNewline()
                        )
                    Audiences.players()
                        .playSound(Sound.sound(SoundEvent.ENTITY_ENDER_DRAGON_GROWL, Sound.Source.MASTER, 1f, 1f))
                    team.members.forEach { it.showTitle(title(Component.text("BED DESTROYED", NamedTextColor.RED), Component.text("You cannot respawn anymore", messageColor))) }
                    activeBeds[team] = false
                }
            }
        }
        eventNode.addListener(PlayerSpawnEvent::class.java) { event ->
            if (event.isFirstSpawn) {
                alivePlayers.add(event.player)
                event.player.gameMode = GameMode.ADVENTURE
            }
        }
        eventNode.addListener(GameStateChangedEvent::class.java) { event ->
            if (event.newState == GameState.RUNNING) {
                players.forEach { player ->
                    player.gameMode = GameMode.SURVIVAL
                    player.inventory.clear()
                }
            }
        }
        eventNode.addListener(PlayerDeathEvent::class.java) { event ->
            val player = event.player
            val attacker = player.lastDamageSource?.attacker

            val team = getModule<TeamModule>().getTeam(player)

            if (attacker is Player) {
                val resourceItems = player.inventory.itemStacks.filter {
                    it.material() in ResourceType.entries.map { type -> type.material }
                }
                resourceItems.forEach {
                    val resourceType = ResourceType.fromMaterial(it.material())!!
                    attacker.inventory.addItemStackFirstOffHand(it)
                    attacker.sendMessage(
                        Component.text("+${it.amount()} ", resourceType.color)
                            .append(resourceType.title)
                    )
                }
            }

            player.inventory.clear()
            getModule<SpectatorModule>().addSpectator(player)

            if (!canRespawn(player)) {
                alivePlayers.remove(player)
                event.chatMessage = event.chatMessage?.appendSpace()?.translate(
                    "game.final_kill",
                    NamedTextColor.RED,
                    TextDecoration.BOLD
                )
                if (attacker is Player) {
                    //Audiences.server().sendMessage(Component.text("Final Kill code is being triggered"))
                    //event.instance.players.forEach {
                    //    it.playSound(
                    //        Sound.sound(
                    //            SoundEvent.ENTITY_LIGHTNING_BOLT_THUNDER,
                    //            Sound.Source.MASTER,
                    //            1f,
                    //            1f
                    //        ), player.position
                    //    )
                    //}
                    //event.instance.players.forEach {
                    //    it.playSound(
                    //        Sound.sound(
                    //            SoundEvent.ENTITY_LIGHTNING_BOLT_IMPACT,
                    //            Sound.Source.MASTER,
                    //            1f,
                    //            0.5f
                    //        ), player.position
                    //    )
                    //}
                    //val lighningBolt = Entity(EntityType.LIGHTNING_BOLT)
                    //lighningBolt.setInstance(event.instance, player.position)
                }
            } else {
                if (attacker is Player) {
                    attacker.playSound(Sound.sound(SoundEvent.BLOCK_NOTE_BLOCK_PLING, Sound.Source.MASTER, 1f, 2f))
                }
            }

            if (team != null) {
                if (alivePlayers.none { getModule<TeamModule>().getTeam(it)?.id == team.id }) {
                    Audiences.server().sendMessage(
                        Component.newline().translate(
                            "game.team.eliminated",
                            NamedTextColor.RED,
                            team.title
                        ).appendNewline()
                    )
                }
            }

            if (alivePlayers.map { getModule<TeamModule>().getTeam(it) }.distinctBy { it?.id }.size == 1) {
                state = GameState.ENDED
                val winningTeam = getModule<TeamModule>().playerTeams[alivePlayers[0].uuid]!!
                Audiences.server().sendMessage(Component.translatable("game.over.message", winningTeam.title))
                winningTeam.members.forEach {
                    it.showTitle(
                        title(
                            Component.translatable("game.over.won", NamedTextColor.GREEN),
                            Component.empty(),
                            DEFAULT_TIMES
                        )
                    )
                }
                Audiences.players().filterAudience { it !in winningTeam.members }.showTitle(
                    title(
                        Component.translatable("game.over.lost", NamedTextColor.RED),
                        Component.empty(),
                        DEFAULT_TIMES
                    )
                )
            }
        }
        eventNode.addListener(PlayerRespawnEvent::class.java) { event ->
            val player = event.player
            val team = getModule<TeamModule>().getTeam(player)

            event.respawnPosition = mapProperties.spawn

            if (canRespawn(player)) {
                var countdown = 5
                MinecraftServer.getSchedulerManager().submitTask {
                    if (countdown == 0) {
                        getModule<SpectatorModule>().removeSpectator(player)
                        player.teleport(team?.respawnPoint() ?: mapProperties.spawn)
                        return@submitTask TaskSchedule.stop()
                    }
                    player.showTitle(
                        title(
                            Component.translatable("game.death.title", NamedTextColor.RED),
                            Component.translatable(
                                "game.death.respawn_subtitle",
                                NamedTextColor.YELLOW,
                                Component.text(countdown, NamedTextColor.RED)
                            ),
                            Times.times(Duration.ZERO, Duration.ofMillis(1100), Duration.ZERO),
                        )
                    )
                    countdown--
                    return@submitTask TaskSchedule.seconds(1)
                }
            } else {
                player.showTitle(
                    title(
                        Component.translatable("game.death.title", NamedTextColor.RED),
                        Component.translatable("game.bedwars.death.bed_broken_subtitle", NamedTextColor.YELLOW),
                        DEFAULT_TIMES
                    )
                )
            }
        }
    }
}