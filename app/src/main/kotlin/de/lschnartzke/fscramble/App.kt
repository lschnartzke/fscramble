package de.lschnartzke.fscramble

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.int
import de.lschnartzke.fscramble.cache.DataCache
import de.lschnartzke.fscramble.scramblers.*
import io.klogging.config.ANSI_CONSOLE
import io.klogging.config.loggingConfiguration
import io.klogging.logger
import kotlinx.coroutines.*
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

    private val scramblers = mapOf(
        "pdf" to PdfScrambler(),
        "odt" to OdfScrambler(),
        "ods" to OdsScrambler(),
        "docx" to DocxScrambler(),
        "xlsx" to XlsxScrambler(),
        "txt" to PlaintextScrambler()
    )
    private val logger = logger<App>()
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined)

    override fun run() {
        val job = scope.launch {
            DataCache.init(dataDirectory)

            if (input.size != output.size) {
                logger.error("--input MUST have same number of arguments as --output")
                return@launch
            }

            if (input.size == 1)
                runSingleFile()
            else {
                runMultipleFiles()
            }
        }

        runBlocking {
            job.join()
            logger.info("Done.")
            Thread.sleep(2000)
            println("Done.")
        }


    }

    private suspend fun runMultipleFiles() {

    }

    private suspend fun runSingleFile() {
        val input = input[0]
        val output = output[0]
        val ifile = File(input)
        val ofile = File(output)
        if (ifile.isFile) {
            scrambleFile(input, output)
        } else if (ifile.isDirectory && ofile.isDirectory) {
            scrambleDirectory(input, output)
        }

    }

    private suspend fun scrambleFile(input: String, output: String) {
        val ifile = File(input)
        scramblers[ifile.extension]?.scramble(input, output, scrambleCount)
    }

    private suspend fun scrambleDirectory(input: String, output: String) = runBlocking {
        val dir = File(input)

        val files = dir.listFiles()
        if (files == null) {
            logger.error("Failed to readdir", "dir" to dir.path)
            return@runBlocking
        }

        val jobs: MutableList<Job> = mutableListOf()
        for (file in files) {
            val job = scope.launch(Dispatchers.IO) {
                scrambleFile(file.absolutePath, output)
            }
            jobs.add(job)
        }

        jobs.forEach {
            try {
                it.join()
            } catch (e: Exception) {
                logger.error("Failed to join coroutine", "error" to e)
            }
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
