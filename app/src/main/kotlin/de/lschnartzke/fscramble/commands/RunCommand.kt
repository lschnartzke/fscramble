package de.lschnartzke.fscramble.commands

import com.charleskorn.kaml.PolymorphismStyle
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.charleskorn.kaml.YamlList
import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.YamlNamingStrategy
import com.charleskorn.kaml.YamlNode
import com.charleskorn.kaml.decodeFromStream
import com.charleskorn.kaml.yamlMap
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import de.lschnartzke.fscramble.config.Configuration
import de.lschnartzke.fscramble.config.RunConfig
import de.lschnartzke.fscramble.runner.AbstractRunner
import de.lschnartzke.fscramble.scramblers.AbstractArchiveScrambler
import de.lschnartzke.fscramble.scramblers.AbstractScrambler
import io.klogging.logger
import jdk.xml.internal.SecuritySupport.readConfig
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileInputStream
import kotlin.system.exitProcess

class RunCommand : CliktCommand() {
    val configFile: String by option("-c", "--config", help = "Configuration file to use, must be YAML").required()
        .validate { File(it).canRead() }
    val mode: String? by argument().optional().help { "THe configuration to run. The specified mode must exist under the 'run' key in the config file" }
    val list: Boolean by option("-l", "--list").flag().help { "If present, list all available modes and exit" }

    private val logger = logger<RunCommand>()

    private fun readConfig(file: String): Configuration {
        val yamlParser = Yaml(
            configuration = Yaml.default.configuration.copy(
                polymorphismStyle = PolymorphismStyle.Property,
                polymorphismPropertyName = "command",
                yamlNamingStrategy = YamlNamingStrategy.KebabCase
            )
        )
        val cfg: Configuration = yamlParser.decodeFromStream(Configuration.serializer(), FileInputStream(file))

        return cfg
    }

    private suspend fun validateConfig(config: Configuration) {
        var error = false
        val validFileTypes = AbstractScrambler.extensions
        val validArchiveTypes = AbstractArchiveScrambler.supportedArchives
        for ((key, configEntry) in config.run.entries) {
            // TODO: Validate correct file and archive types for both scramble and create commands
            when (configEntry) {
                is RunConfig.Create -> {
                    if (!validFileTypes.containsAll(configEntry.fileTypes)) {
                        logger.error("Invalid file type in configuration: ${configEntry.fileTypes} (valid: $validFileTypes)")
                        error = true
                    }

                    if (!validArchiveTypes.containsAll(configEntry.archiveTypes)) {
                        logger.error("Invalid archive type in configuration: ${configEntry.archiveTypes} (valid: $validArchiveTypes)")
                        error = true
                    }
                }
                is RunConfig.Scramble -> {
                    if (!validArchiveTypes.containsAll(configEntry.archiveTypes)) {
                        logger.error("Invalid archive type in configuration: ${configEntry.archiveTypes} (valid: $validArchiveTypes)")
                        error = true
                    }
                }
            }
        }

        if (error) {
            logger.info("Cannot proceed with errors, exiting...")
            exitProcess(1)
        }
    }

    override fun run(): Unit = runBlocking {
        logger.info("Starting...")
        val config = readConfig(configFile)

        if (list || mode == null) {
            val modes = config.run.keys.toCollection(mutableListOf())
            logger.info("Available modes: $modes")
            exitProcess(0)
        }

        if (!config.run.containsKey(mode)) {
            logger.error("Mode $mode does not exist, available modes: ${config.run.keys}")
            exitProcess(1)
        }

        val runCfg = config.run[mode]!!
        logger.info("Running...")

        val runner = AbstractRunner.fromRunConfig(runCfg)
        runner.run()
        logger.info("Done.")
    }
}