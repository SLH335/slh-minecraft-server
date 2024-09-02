package xyz.hafemann.slhserver

import io.github.cdimascio.dotenv.dotenv
import io.github.togar2.pvp.MinestomPvP
import net.minestom.server.MinecraftServer
import net.minestom.server.extras.velocity.VelocityProxy
import net.minestom.server.network.packet.client.play.ClientSignedCommandChatPacket
import org.slf4j.LoggerFactory
import kotlin.system.measureTimeMillis

private val logger = LoggerFactory.getLogger("Main")

val dotenv = dotenv()

fun main() {
    val time = measureTimeMillis(::start)
    logger.info("Game Server started in ${time}ms")
}

private fun start() {
    val minecraftServer = MinecraftServer.init()

    MinestomPvP.init()

    // Enable Velocity proxy with secret from environment variable
    val velocitySecret = (dotenv["VELOCITY_SECRET"] ?: "").ifEmpty { error("No Velocity secret provided") }
    VelocityProxy.enable(velocitySecret)

    // Execute commands sent through Velocity proxy
    MinecraftServer.getPacketListenerManager()
        .setPlayListener(ClientSignedCommandChatPacket::class.java) { packet, player ->
            MinecraftServer.getCommandManager().execute(player, packet.message)
        }

    val port = dotenv["PORT"].toIntOrNull() ?: 25565
    minecraftServer.start("0.0.0.0", port)
    logger.info("Started Minecraft Server on port $port")
}