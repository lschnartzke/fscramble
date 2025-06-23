package de.lschnartzke.fscramble.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.long
import com.github.ajalt.clikt.parameters.types.uint
import de.lschnartzke.fscramble.config.RunConfig
import de.lschnartzke.fscramble.config.Size
import de.lschnartzke.fscramble.runner.AbstractRunner
import de.lschnartzke.fscramble.scramblers.AbstractScrambler
import io.klogging.logger
import kotlinx.coroutines.*
import java.io.File

class CreateCommand : CliktCommand() {
    private val targetDirectory: String by option().required().help { "Directory in which to store the created files" }
    private val dataDirectory: String by option().required().help { "Directory used to obtain data (images/text files) to use when filling new files with content" }
    private val count: UInt? by option().uint().help { "The number of files (in total) to create. Conflicts with --size" }
    private val fileTypes: List<String> by option().multiple(default = AbstractScrambler.extensions.toList()).help { "What types of files to create. The list consists of the respective extensions. You can get a list of all supported" +
            "file types using --list-file-types" }
    private val size: Long? by option().long().help { "Specifies the amount of data to create." +
            " This will continue running until the total amount of space occupied by created files equals the specified size." +
            " Conflicts with --count" +
            "NOTE: The total size will always be slightly more than specified. This is in part due to how the files are created as well as the general challenge" +
            "of creating files that use different encoding mechanisms in their format. Thus, --size is to be viewed as a lower bound of the total size" +
            "and the actual size is expected to be a few bytes to megabytes more." }.validate { it > 0 }
    private val jobs: Int by option().int().default(Runtime.getRuntime().availableProcessors()).help { "Amount of jobs to run in parallel. Determines how " +
            "many jobs will be created and run in parallel if using --size. Defaults to the number of available processors on the system. Higher numbers will likely increase the" +
            "delta between the target size and amount of data actually generated." }

    private val logger = logger<CreateCommand>()

    fun validateArgs() {
        val validFileTypes = AbstractScrambler.extensions.toList()
        for (fileType in fileTypes) {
            if (!validFileTypes.contains(fileType)) {
                throw IllegalArgumentException("Invalid file type: $fileType")
            }
        }

        if (count != null && size != null) {
            throw IllegalArgumentException("--size AND --count conflict, use only one")
        } else if (count == null && size == null) {
            throw IllegalArgumentException("either --count OR --size must be specified")
        }

        val outpath = File(targetDirectory)
        if (!outpath.exists()) {
            outpath.mkdirs()
        } else if (!outpath.isDirectory) {
            throw IllegalArgumentException("--target-directory must be a directory")
        }
    }

    override fun run() = runBlocking {
        validateArgs()
        val runConfig = RunConfig.Create(
            targetDirectory = targetDirectory,
            dataDirectory = dataDirectory,
            size = Size(size),
            count = count?.toLong(),
            jobs = jobs,
            fileTypes = fileTypes
        )

        val runner = AbstractRunner.fromRunConfig(runConfig)
        runner.run()
    }
}