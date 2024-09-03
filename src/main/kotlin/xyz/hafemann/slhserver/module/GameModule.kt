package xyz.hafemann.slhserver.module

import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import org.slf4j.LoggerFactory
import xyz.hafemann.slhserver.game.Game
import kotlin.reflect.full.findAnnotation

abstract class GameModule {
    protected val logger = LoggerFactory.getLogger(javaClass)

    open val eventPriority = 0

    lateinit var eventNode: EventNode<Event>

    abstract fun initialize(parent: Game, eventNode: EventNode<Event>)

    open fun deinitialize() {}

    open fun getRequiredDependencies() = this::class.findAnnotation<DependsOn>()?.dependencies ?: emptyArray()
    open fun getSoftDependencies() = this::class.findAnnotation<SoftDependsOn>()?.dependencies ?: emptyArray()

    fun getDependencies() = arrayOf(*getRequiredDependencies(), *getSoftDependencies())
}