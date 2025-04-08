package de.lschnartzke.fscramble

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.int
import de.lschnartzke.fscramble.cache.DataCache
import de.lschnartzke.fscramble.scramblers.DocxScrambler
import de.lschnartzke.fscramble.scramblers.OdfScrambler
import de.lschnartzke.fscramble.scramblers.OdsScrambler
import de.lschnartzke.fscramble.scramblers.PdfScrambler
import io.klogging.config.ANSI_CONSOLE
import io.klogging.config.loggingConfiguration
import io.klogging.logger
import kotlinx.coroutines.runBlocking
import java.io.File

/**
 * Scramble one or multiple files or directories
 *
 * needs target and source destinations
 */
class App : CliktCommand() {
    override val printHelpOnEmptyArgs = true
    private val dataDirectory: String by option().required().help("Directory containing data to use for scrambling (e.g. images to put into PDFs")
    private val input: List<String> by option().multiple().help("One or more input files. If the file is a directory, all files in the directory will be scrambled")
    private val output: List<String> by option().multiple().help("Where to output scrambeld files or directories. MUST be the same length as input")
    private val scrambleCount: Int by option().int().default(5).help("Amount of scramble operations to apply")

    override fun run() {
        DataCache.init(dataDirectory)

        if (input.size == 1 && output.size == 1)
            runSingleFile()
    }

    private fun runSingleFile() {
        val scramblers = mapOf(
            "pdf" to PdfScrambler(),
            "odt" to OdfScrambler(),
            "ods" to OdsScrambler(),
            "docx" to DocxScrambler()
        )
        runBlocking {
            val ifile = File(input[0])

            scramblers[ifile.extension]?.scramble(input[0], output[0], scrambleCount)
        }

    }
}

fun main(args: Array<String>) = runBlocking {
    loggingConfiguration {
        ANSI_CONSOLE()
    }

    logger("main").info("Starting up...")
    App().main(args)
}
