package de.lschnartzke.fscramble.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.uint

class CreateCommand : CliktCommand() {
    val targetDirectory: String by option().required().help { "Directory in which to store the created files" }
    val dataDirectory: String by option().required().help { "Directory used to obtain data (images/text files) to use when filling new files with content" }
    val count: UInt? by option().uint().help { "The number of files (in total) to create. Conflicts with --size" }
    val size: UInt? by option().uint().help { "Specifies the amount of data to create. This will continue running until the total amount of space occupied by created files equals the specified size. Conflicts with --count" }

    override fun run() {
    }
}