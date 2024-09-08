package xyz.hafemann.slhserver.service.database

import org.jetbrains.exposed.sql.Database
import org.slf4j.LoggerFactory
import xyz.hafemann.slhserver.dotenv

object Database {
    private val logger = LoggerFactory.getLogger(this::class.simpleName)

    private lateinit var db: Database

    fun configureDatabase() {
        db = Database.connect(
            driver = dotenv["DB_DRIVER"],
            url = dotenv["DB_URL"],
            user = dotenv["DB_USER"],
            password = dotenv["DB_PASSWORD"],
        )
        logger.info("Configured database connection")
    }
}