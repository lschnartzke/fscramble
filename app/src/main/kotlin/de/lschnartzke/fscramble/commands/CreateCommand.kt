package de.lschnartzke.fscramble.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.uint
import de.lschnartzke.fscramble.cache.DataCache
import java.util.Objects

class CreateCommand : CliktCommand() {
    private val targetDirectory: String by option().required().help { "Directory in which to store the created files" }
    private val dataDirectory: String by option().required().help { "Directory used to obtain data (images/text files) to use when filling new files with content" }
    private val count: UInt? by option().uint().help { "The number of files (in total) to create. Conflicts with --size" }
    private val size: UInt? by option().uint().help { "Specifies the amount of data to create. This will continue running until the total amount of space occupied by created files equals the specified size. Conflicts with --count" }

    private var sizeCreated: UInt = 0u

    fun validateArgs() {
        if (count != null && size != null) {
            throw IllegalArgumentException("--size and --count conflict, use only one")
        }
    }

    fun loadDataCache(): DataCache {
    }

    override fun run() {
        validateArgs()
        DataCache.init(dataDirectory)


    }
}