package de.lschnartzke.fscramble

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option

class App : CliktCommand() {
    override val printHelpOnEmptyArgs = true
    val dataDirectory: String? by option().help("Directory containing data to use for scrambling (e.g. images to put into PDFs")
    val targetDirectory: String? by option().help("Directory in which to scramble files")

    override fun run() {
        println("target-directory: ${targetDirectory ?: ""}")
    }
}

fun main(args: Array<String>) {
    App().main(args)
}
