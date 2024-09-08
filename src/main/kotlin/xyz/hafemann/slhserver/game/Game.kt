package xyz.hafemann.slhserver.game

import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import net.minestom.server.event.Event
import net.minestom.server.event.EventFilter
import net.minestom.server.event.EventNode
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.instance.LightingChunk
import net.minestom.server.instance.anvil.AnvilLoader
import org.slf4j.LoggerFactory
import xyz.hafemann.slhserver.event.GameStateChangedEvent
import xyz.hafemann.slhserver.module.GameModule
import xyz.hafemann.slhserver.module.ModuleHolder
import xyz.hafemann.slhserver.module.config.GameConfigModule
import xyz.hafemann.slhserver.util.sendTranslatedError
import xyz.hafemann.slhserver.util.translateError
import java.nio.file.Paths
import java.util.concurrent.CopyOnWriteArrayList
import java.util.function.Predicate
import kotlin.reflect.jvm.jvmName

abstract class Game(val name: String, val mode: String, val map: String) : ModuleHolder() {
    private val logger = LoggerFactory.getLogger(this::class.java)

    var state: GameState = GameState.SERVER_STARTING
        set(value) {
            eventNode.call(GameStateChangedEvent(this, field, value))
            field = value
        }

    var maxPlayers = -1 // -> no limit by defalut

    internal val players: MutableList<Player> = CopyOnWriteArrayList()

    lateinit var instance: InstanceContainer
        private set

    lateinit var properties: GameProperties
    lateinit var modeProperties: GameModeProperties
    lateinit var mapProperties: GameMapProperties

    protected val eventNode = EventNode.all("$name-$mode-$map")

    override fun <T : GameModule> register(module: T, filter: Predicate<Event>) {
        val eventNode = createEventNode(module, filter)

        module.eventNode = eventNode
        module.initialize(this, eventNode)
        logger.debug("Registered module {}", module::class.simpleName)
    }

    private fun createEventNode(module: GameModule, filter: Predicate<Event>): EventNode<Event> {
        val child = EventNode.event(module::class.simpleName.orEmpty(), EventFilter.ALL, filter)
        child.priority = module.eventPriority
        eventNode.addChild(child)
        return child
    }

    fun init() {
        use(GameConfigModule())

        loadMap()

        maxPlayers = modeProperties.maxPlayers

        initialize()

        checkUnmetDependencies()

        logger.debug("Initializing game with modules: {}", modules.map { it::class.simpleName ?: it::class.jvmName })

        state = GameState.WAITING

        MinecraftServer.getGlobalEventHandler().addChild(eventNode)
    }

    protected abstract fun initialize()

    private fun loadMap() {
        val path = try {
            Paths.get("worlds/$name/$mode/$map")
        } catch (e: Throwable) {
            logger.error("Could not find $name map $map for mode $mode")
            return
        }
        instance = MinecraftServer.getInstanceManager().createInstanceContainer()
        instance.chunkLoader = AnvilLoader(path)
        instance.setChunkSupplier(::LightingChunk)
        instance.time = 3000
        instance.timeRate = 0
    }

    fun addPlayer(player: Player) {
        if ((maxPlayers > 0) && (players.size >= maxPlayers) && !players.any { it.uuid == player.uuid }) {
            player.sendTranslatedError("game.error.full")
            player.kick(translateError("game.error.full"))
            return
        }
        if (!players.any { it.uuid == player.uuid }) {
            players.add(player)
        }
    }
}