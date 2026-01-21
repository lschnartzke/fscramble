package de.lschnartzke.fscramble.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.*
import de.lschnartzke.fscramble.config.loadConfigurationFromFile
import io.klogging.logger
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.system.exitProcess

class RunCommand : CliktCommand() {
    val configFile: String by option("-c", "--config", help = "Configuration file to use, must be YAML").required()
        .validate { File(it).canRead() }
    val mode: String? by argument().optional().help { "THe configuration to run. The specified mode must exist under the 'run' key in the config file" }
    val list: Boolean by option("-l", "--list").flag().help { "If present, list all available modes and exit" }

    private val logger = logger<RunCommand>()

    override fun run(): Unit = runBlocking {
        val config = loadConfigurationFromFile(File(configFile))

        if (list) {
            val availableConfigs = config.runConfigs.map { it.name }
            logger.info("Available configuration: $availableConfigs")

            exitProcess(1)
        }



    }
}