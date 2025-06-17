package de.lschnartzke.fscramble.runner

import de.lschnartzke.fscramble.cache.DataCache
import de.lschnartzke.fscramble.config.RunConfig
import de.lschnartzke.fscramble.scramblers.AbstractScrambler
import io.klogging.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File

class ScrambleRunner(private val config: RunConfig.Scramble) : AbstractRunner() {
    private val log = logger<ScrambleRunner>()
    private val inputDirectory = config.inputDirectory
    private val targetDirectory = config.targetDirectory
    private val scrambleCount = config.count

    val scope = CoroutineScope(Dispatchers.Default)


    override fun run() = runBlocking {
        DataCache.init(config.dataDirectory)

        scrambleDirectory(inputDirectory, targetDirectory)
        return@runBlocking

    }

    private suspend fun scrambleDirectory(input: String, output: String) {
        val dir = File(input)
        if (!dir.isDirectory) {
            log.error("Expected ${dir.absolutePath} to be a directory")
            return
        }

        val files = dir.listFiles()
        if (files == null) {
            log.error("Failed to readdir", "dir" to dir.path)
            return
        }

        val jobs: MutableList<Job> = mutableListOf()
        for (file in files) {
            val job = scope.launch {
                if (file.isDirectory) {
                    val outdir = File(output, file.name)
                    if (!outdir.exists())
                        outdir.mkdirs()

                    scrambleDirectory(file.absolutePath, outdir.absolutePath)
                } else {
                    scrambleFile(file.absolutePath, output)
                }
            }
            jobs.add(job)
        }

        jobs.forEach {
            try {
                it.join()
            } catch (e: Exception) {
                log.e("Failed to join coroutine", "error" to e)
            }
        }
    }

    private suspend fun scrambleFile(file: String, output: String) {
        val ifile = File(file)

        try {
            AbstractScrambler.scramblerExtensionMap[ifile.extension]?.scramble(file, output, scrambleCount.toInt())
        } catch (e: Exception) {
            log.e("Failed to scramble file $file", "error" to e)
        }
    }
}