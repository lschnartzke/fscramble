package de.lschnartzke.fscramble

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.int
import de.lschnartzke.fscramble.scramblers.PdfScrambler
import kotlinx.coroutines.runBlocking

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
        if (input.size == 1 && output.size == 1)
            runSingleFile()
    }

    private fun runSingleFile() {
        val scrambler = PdfScrambler(dataDirectory)
        runBlocking {
            scrambler.init()

            scrambler.scramble(input[0], output[0], scrambleCount)
        }

    }
}

fun main(args: Array<String>) {
    App().main(args)
}
