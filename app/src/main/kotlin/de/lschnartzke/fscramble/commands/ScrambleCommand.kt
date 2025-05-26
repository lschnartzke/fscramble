package de.lschnartzke.fscramble.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.uint
import kotlinx.coroutines.runBlocking

class ScrambleCommand : CliktCommand() {
    val targetDirectory: String by option().required().help { "Directory in which to scramble (or create). Must be present" }
    val dataDirectory: String by option().required().help { "Directory which contains the content (images/text) to use during scrambling" }
    val count: UInt by option().uint().default(20u).help { "Amount of scramble actions (add/remove text/page) to perform *per file*"}
    val create: Boolean by option().flag().help { "Also create files. This is different from the create command, as it will randomly create files or none at all" }
    val createMin: UInt by option().uint().default(0u).help { "Minimum amount of files to create (only applied if --create is present)" }
    val createMax: UInt by option().uint().default(0u).help { "Maximum amount of files to create (only applied if --create is present)" }

    fun validateArgs() {
        if (create && (createMin > createMax)) {
            throw IllegalArgumentException("--create-min must be smaller than --create-max")
        }
    }

    override fun run()  {
        validateArgs()
    }
}