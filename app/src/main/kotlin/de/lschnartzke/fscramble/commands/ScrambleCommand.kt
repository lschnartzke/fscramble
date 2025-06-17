package de.lschnartzke.fscramble.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.long
import com.github.ajalt.clikt.parameters.types.uint
import de.lschnartzke.fscramble.config.RunConfig
import de.lschnartzke.fscramble.runner.AbstractRunner
import kotlinx.coroutines.runBlocking

class ScrambleCommand : CliktCommand() {
    val inputDirectory: String by option("-i", "--input", "--input-directory").required().help { "Directory from which to scramble files. The files will be read, scrambled and written to --target-directory" }
    val targetDirectory: String by option("-t", "--output", "--target-directory").required().help { "Directory in which to scramble (or create). Must be present" }
    val dataDirectory: String by option("-d", "--data", "--data-directory").required().help { "Directory which contains the content (images/text) to use during scrambling" }
    val count: Long by option("-c", "--count").long().default(20).help { "Amount of scramble actions (add/remove text/page) to perform *per file*"}
    val create: Boolean by option("-C", "--create").flag().help { "Also create files. This is different from the create command, as it will randomly create files or none at all" }
    val createMin: Long by option("--min", "--create-min").long().default(0).help { "Minimum amount of files to create (only applied if --create is present)" }.validate { it >= 0 }
    val createMax: Long by option("--max", "--create-max").long().default(0).help { "Maximum amount of files to create (only applied if --create is present)" }.validate { it >= 0 }

    fun validateArgs() {
        if (create && (createMin > createMax)) {
            throw IllegalArgumentException("--create-min must be smaller than --create-max")
        }
    }

    override fun run()  {
        validateArgs()

        val runConfig = RunConfig.Scramble(
            inputDirectory = inputDirectory,
            targetDirectory = targetDirectory,
            dataDirectory = dataDirectory,
            count = count,
            create = create,
            createMin = createMin,
            createMax = createMax,
        )

        val runner = AbstractRunner.fromRunConfig(runConfig)
        runner.run()
    }
}