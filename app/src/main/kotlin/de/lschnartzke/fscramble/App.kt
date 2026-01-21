package de.lschnartzke.fscramble

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.int
import de.lschnartzke.fscramble.cache.DataCache
import de.lschnartzke.fscramble.commands.RunCommand
import de.lschnartzke.fscramble.scramblers.*
import io.klogging.config.ANSI_CONSOLE
import io.klogging.config.loggingConfiguration
import io.klogging.logger
import kotlinx.coroutines.*
import java.io.File
import kotlin.system.exitProcess

/**
 * Scramble one or multiple files or directories
 *
 * needs target and source destinations
 */
class App : CliktCommand() {
    private val listFileTypes: Boolean by option("--list-file-types").flag().help { "List all available file types and exit" }
    override val invokeWithoutSubcommand: Boolean = true

    private val logger = logger<App>()

    override fun run() {
        if (listFileTypes) {
            listAvailableFileTypes()
            exitProcess(0)
        }

        Library().check()
    }

    private fun listAvailableFileTypes() = runBlocking {
        val fileTypes = AbstractScrambler.extensions
        logger.info("Available file types: {fileTypes}", fileTypes)
    }
}

fun main(args: Array<String>) = runBlocking {
    loggingConfiguration {
        ANSI_CONSOLE()
    }

    logger("main").info("Starting up...")
    App().subcommands(RunCommand()).main(args)
}
