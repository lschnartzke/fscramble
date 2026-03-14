package de.lschnartzke.fscramble.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import de.lschnartzke.fscramble.Library
import java.io.File

class LibraryCommand : CliktCommand() {
    private val dataDirectory: String by option("-d", "--data-directory", help="Data Directory containing sample data").required().validate { File(it).isDirectory }
    override val invokeWithoutSubcommand = true

    override fun run(): Unit {
    }
}