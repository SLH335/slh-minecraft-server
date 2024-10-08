package xyz.hafemann.slhserver.game

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.Serializable
import net.minestom.server.coordinate.BlockVec
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.ItemEntity
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import org.slf4j.LoggerFactory
import xyz.hafemann.slhserver.game.bedwars.Bedwars
import xyz.hafemann.slhserver.game.lobby.Lobby
import xyz.hafemann.slhserver.module.minigame.ResourceType
import java.nio.file.Paths
import java.time.Duration
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.reflect.full.primaryConstructor

object GameLoader {
    private val logger = LoggerFactory.getLogger(this::class.simpleName)

    private val games = mutableMapOf(
        "lobby" to Lobby::class.java,
        "bedwars" to Bedwars::class.java
    )

    private val gameProperties by lazy {
        val paths = try {
            Paths.get("games").listDirectoryEntries().filter { it.isRegularFile() }
        } catch (e: Throwable) {
            error("Game properties directory does not exist")
        }
        paths.map { path ->
            try {
                Yaml.default.decodeFromString(GameProperties.serializer(), path.toFile().readText())
            } catch (e: Throwable) {
                e.printStackTrace()
                error("Failed to load game properties from file ${path.fileName}")
            }
        }
    }

    private fun getMapProperties(gameName: String, mode: String, mapName: String): GameMapProperties {
        val path = Paths.get("worlds/$gameName/$mode/$mapName/map-config.yml")
        return Yaml.default.decodeFromString(GameMapProperties.serializer(), path.toFile().readText())
    }

    fun runGame(name: String, mode: String = name, mapName: String = name): Game {
        val constructor = games[name]?.kotlin?.primaryConstructor
            ?: error("Game class for game $name not found or improperly loaded")
        val properties = gameProperties.find { it.id == name } ?: error("Game properties for game $name not found")
        if (mode !in properties.modes.map { it.id }) error("Invalid mode specified: $mode")
        val mapProperties = getMapProperties(name, mode, mapName)
        if (mapProperties.id != mapName) error("Invalid map specified: $mapName")
        return when (constructor.parameters.size) {
            0 -> constructor.call()
            1 -> constructor.call(mapName)
            2 -> constructor.call(mode, mapName)
            else -> error("Unexpected constructur format: $constructor")
        }.apply { init() }
    }
}

@Serializable
data class GameProperties(
    val id: String,
    val name: String,
    val mainclass: String,
    val modes: List<GameModeProperties> = listOf(GameModeProperties(id, name)),
)

@Serializable
data class GameModeProperties(
    val id: String,
    val name: String,
    val teamCount: Int = -1,
    val teamSize: Int = -1,
) {
    val maxPlayers: Int
        get() {
            if (teamCount == -1 || teamSize == -1) return -1
            return teamCount * teamSize
        }
}

@Serializable
data class GameMapProperties(
    val id: String,
    val name: String,
    private val spawnpos: PosProperties,
    val radius: Int,
    val npcs: List<NPCProperties> = listOf(),
    val teams: List<TeamProperties> = listOf(),
    val generators: List<GeneratorProperties> = listOf(),
) {
    val spawn get() = spawnpos.pos()
}

@Serializable
data class NPCProperties(
    private val entitytype: String,
    val name: String,
    private val position: PosProperties,
    val skin: SkinProperties? = null,
    val action: String,
) {
    val type: EntityType get() = EntityType.fromNamespaceId("minecraft:${entitytype.lowercase()}")
    val pos get() = position.pos()
}

@Serializable
data class TeamProperties(
    val id: String,
    private val spawnpos: PosProperties,
) {
    val pos get() = spawnpos.pos()
}

@Serializable
data class PosProperties(
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float = 0f,
    val pitch: Float = 0f,
) {
    fun pos(): Pos {
        return Pos(x, y, z, yaw, pitch)
    }
}

@Serializable
data class GeneratorProperties(
    private val type: String,
    private val position: PosProperties,
) {
    val pos: Pos
        get() {
            return position.pos()
        }

    val resource: ResourceType
        get() {
            return ResourceType.valueOf(type.uppercase())
        }

    fun dropResource(instance: InstanceContainer) {
        val droppedItems = instance.entities.filter {
            it is ItemEntity
                    && it.position.distance(pos) < 2
                    && it.itemStack.material() == resource.material
        }.map { (it as ItemEntity).itemStack.amount() }.reduceOrNull { acc, i -> acc + i }

        if (droppedItems == null || droppedItems < resource.spawnLimit) {
            val itemEntity = ItemEntity(ItemStack.of(resource.material))
            itemEntity.setInstance(instance, pos)
        }
    }
}

@Serializable
data class SkinProperties(
    val textures: String,
    val signature: String,
)