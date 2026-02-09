package de.lschnartzke.fscramble.commands.library

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.path
import com.github.ajalt.clikt.parameters.types.uint
import de.lschnartzke.fscramble.Library
import java.io.File
import java.nio.file.Path

class CreateFilesCommand : CliktCommand(){
    val dataDirectory: String by option("-d", "--data-directory", help="Data directory to source sample data from").required().validate {
        File(it).isDirectory
    }

    val extension: String by option("-e", "--extension", help="File type to create. Use --list-file-types to get a list of supported file types").required()
    val count: Int by option("-c", "--count", help="Number of files to create").int().required()
    val outpath: Path by option("-o", "--outpath", help="where to store created files (will be created if missing)").path().required()
    val scrambleCount: Int by option("-s", "--scramble-count", help="How many scramble actions to perform per file (default: 50)").int().default(50)

    override fun run() {
        val lib = Library()
        lib.loadDataDirectory(File(dataDirectory).toPath())
        lib.createFiles(extension, outpath, scrambleCount, count)
    }
}