package xyz.hafemann.slhserver.service

import net.kyori.adventure.key.Key
import net.kyori.adventure.translation.GlobalTranslator
import net.kyori.adventure.translation.TranslationRegistry
import net.minestom.server.adventure.MinestomAdventure
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.hafemann.slhserver.util.DEFAULT_LOCALE
import xyz.hafemann.slhserver.util.NAMESPACE
import java.util.Locale
import java.util.Properties
import java.util.PropertyResourceBundle

object Internationalization {
    private val logger: Logger by lazy {
        LoggerFactory.getLogger(this::class.java)
    }

    fun registerTranslations() {
        MinestomAdventure.AUTOMATIC_COMPONENT_TRANSLATION = true

        val classLoader = Internationalization::class.java.classLoader
        val registry = TranslationRegistry.create(Key.key(NAMESPACE, "i18n"))
        val translations = Properties().apply {
            load(classLoader.getResourceAsStream("i18n.properties"))
        }
        val languages = translations.keys.map { it.toString() }.filter { it.startsWith("lang_") }
        for (language in languages) {
            val path = translations.getProperty(language)
            val locale = Locale.forLanguageTag(language.substringAfter("lang_"))
            val bundle = PropertyResourceBundle(classLoader.getResourceAsStream(path))
            registry.registerAll(locale, bundle, true)
            logger.debug(
                "Registered language {} (locale: {}) from file {}",
                language,
                locale,
                translations.getProperty(language)
            )
        }
        registry.defaultLocale(DEFAULT_LOCALE)

        GlobalTranslator.translator().addSource(registry)
    }
}