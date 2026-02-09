package de.lschnartzke.fscramble.commands.library

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.path
import de.lschnartzke.fscramble.Library
import java.io.File
import java.nio.file.Path

class ScrambleFilesCommand : CliktCommand() {
    val dataDirectory: String by option("-d", "--data-directory", help="Data directory to source sample data from").required().validate {
        File(it).isDirectory
    }

    val files: List<File> by option("-i", "--inpath", help="Directory to scramble").file().multiple()
    val outpath: Path by option("-o", "--outpath", help="where to store created files (will be created if missing)").path().required()
    val scrambleCount: Int by option("-s", "--scramble-count", help="How many scramble actions to perform per file (default: 50)").int().default(50)

    override fun run() {
        val lib = Library()
        lib.loadDataDirectory(File(dataDirectory).toPath())
        lib.scrambleFiles(files.toTypedArray(), outpath, scrambleCount)
    }
}