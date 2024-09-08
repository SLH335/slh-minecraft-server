package xyz.hafemann.slhserver

import io.github.cdimascio.dotenv.dotenv
import io.github.togar2.pvp.MinestomPvP
import net.minestom.server.MinecraftServer
import net.minestom.server.command.builder.Command
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerSpawnEvent
import net.minestom.server.extras.velocity.VelocityProxy
import net.minestom.server.instance.block.BlockHandler
import net.minestom.server.network.packet.client.play.ClientSignedCommandChatPacket
import net.minestom.server.permission.Permission
import org.slf4j.LoggerFactory
import xyz.hafemann.slhserver.command.*
import xyz.hafemann.slhserver.game.Game
import xyz.hafemann.slhserver.game.GameLoader
import xyz.hafemann.slhserver.service.BedBlockHandler
import xyz.hafemann.slhserver.service.EnderchestHandler
import xyz.hafemann.slhserver.service.Internationalization
import xyz.hafemann.slhserver.service.SLHPlayer
import xyz.hafemann.slhserver.util.sendTranslatedError
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis

private val logger = LoggerFactory.getLogger("Main")

val dotenv = dotenv()

lateinit var game: Game

fun main() {
    val time = measureTimeMillis(::start)
    logger.info("Game Server started in ${time}ms")
}

private fun start() {
    val minecraftServer = MinecraftServer.init()

    MinestomPvP.init()

    MinecraftServer.getConnectionManager().setPlayerProvider(::SLHPlayer)

    // Enable Velocity proxy with secret from environment variable
    val velocitySecret = (dotenv["VELOCITY_SECRET"] ?: "").ifEmpty { error("No Velocity secret provided") }
    VelocityProxy.enable(velocitySecret)
    MinecraftServer.setCompressionThreshold(0)

    // Execute commands sent through Velocity proxy
    MinecraftServer.getPacketListenerManager()
        .setPlayListener(ClientSignedCommandChatPacket::class.java) { packet, player ->
            MinecraftServer.getCommandManager().execute(player, packet.message)
        }

    // Register commands
    registerCommands()

    // Register listeners
    registerListeners()

    // Register block handlers
    registerBlockHandlers()

    // Register translations
    Internationalization.registerTranslations()

    val port = dotenv["PORT"].toIntOrNull() ?: 25565
    minecraftServer.start("0.0.0.0", port)
    logger.info("Started Minecraft Server on port $port")

    game = try {
        GameLoader.runGame(dotenv["GAME"], dotenv["GAME_MODE"], dotenv["GAME_MAP"])
    } catch (e: Throwable) {
        logger.info("There was an error initializing the game. Shutting down...")
        e.printStackTrace()
        MinecraftServer.stopCleanly()
        exitProcess(1)
    }
    logger.info("Started game ${game.properties.name}")

    val eventHandler = MinecraftServer.getGlobalEventHandler()

    eventHandler.addListener(AsyncPlayerConfigurationEvent::class.java) { event ->
        val player = event.player

        player.addPermission(Permission("command.game"))
        if (player.username == "_BigChungus_") {
            player.permissionLevel = 4
            player.addPermission(Permission("*"))
        }
        //    player.permissionLevel = 3
        //    player.addPermission(Permission("command.*"))
        //}

        event.spawningInstance = game.instance
        player.respawnPoint = game.mapProperties.spawn
        game.addPlayer(player)
    }

    eventHandler.addListener(PlayerSpawnEvent::class.java) { event ->
        if (!event.isFirstSpawn) return@addListener
        event.player.isEnableRespawnScreen = false
    }
}

private fun registerCommands() {
    val commandManager = MinecraftServer.getCommandManager()

    val commands = listOf<Command>(
        AttributeCommand("attribute"),
        ClearCommand("clear"),
        FlyCommand("fly"),
        GameCommand("game"),
        GamemodeCommand("gamemode", "gm"),
        GiveCommand("give"),
        HealCommand("heal"),
        //InvseeCommand("invsee"),
        KillCommand("kill"),
        PingCommand("ping", "latency"),
        StopCommand("stop"),
        TeleportCommand("teleport", "tp"),
    )
    commands.forEach { commandManager.register(it) }

    commandManager.setUnknownCommandCallback { sender, command ->
        sender.sendTranslatedError("command.error.unknown", command)
    }

    logger.debug("Registered ${commands.size} commands")
}

private fun registerListeners() {
    BedBlockHandler.Listener()
}

private fun registerBlockHandlers() {
    val blockHandlers = listOf<BlockHandler>(
        BedBlockHandler()
    )
    blockHandlers.forEach { MinecraftServer.getBlockManager().registerHandler(it.namespaceId) { it } }

    logger.debug("Registered ${blockHandlers.size} block handlers")
}