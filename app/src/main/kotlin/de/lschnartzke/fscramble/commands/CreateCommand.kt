package de.lschnartzke.fscramble.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.defaultLazy
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.long
import com.github.ajalt.clikt.parameters.types.uint
import de.lschnartzke.fscramble.cache.DataCache
import de.lschnartzke.fscramble.scramblers.AbstractScrambler
import io.klogging.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import org.apache.commons.lang3.RandomStringUtils
import java.io.File
import java.util.Objects
import java.util.Random
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.ReentrantLock
import kotlin.time.Duration

class CreateCommand : CliktCommand() {
    private val targetDirectory: String by option().required().help { "Directory in which to store the created files" }
    private val dataDirectory: String by option().required().help { "Directory used to obtain data (images/text files) to use when filling new files with content" }
    private val count: UInt? by option().uint().help { "The number of files (in total) to create. Conflicts with --size" }
    private val size: Long? by option().long().help { "Specifies the amount of data to create." +
            " This will continue running until the total amount of space occupied by created files equals the specified size." +
            " Conflicts with --count" +
            "NOTE: The total size will always be slightly more than specified. This is in part due to how the files are created as well as the general challenge" +
            "of creating files that use different encoding mechanisms in their format. Thus, --size is to be viewed as a lower bound of the total size" +
            "and the actual size is expected to be a few bytes to megabytes more." }.validate { it > 0 }
    private val jobs: Int by option().int().default(Runtime.getRuntime().availableProcessors()).help { "Amount of jobs to run in parallel. Determines how " +
            "many jobs will be created and run in parallel if using --size. Defaults to the number of available processors on the system. Higher numbers will likely increase the" +
            "delta between the target size and amount of data actually generated." }

    private val log = logger<CreateCommand>()

    fun validateArgs() {
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

    override fun run() {
        runBlocking {
            validateArgs()
            DataCache.init(dataDirectory)
            AbstractScrambler.overrideScrambleActions(AbstractScrambler.ScrambleAction.ADD_TEXT,
                AbstractScrambler.ScrambleAction.ADD_MEDIA)

            if (count != null) {
                createFilesUsingCount()
            } else {
                createFilesUsingSize()
            }
        }
    }

    private suspend fun createFilesUsingCount() {
        // SAFETY: Can't be null, we've checked before calling this function (I think)
        var remaining = count!!

        val jobs = mutableListOf<Job>()
        val scope = CoroutineScope(Dispatchers.IO)
        while (remaining-- > 0u) {
            jobs.add(scope.launch {
                createFile()
            })
        }

        jobs.forEach { it.join() }
    }

    private suspend fun createFilesUsingSize() {
        // SAFETY: Can't be null, we've checked before calling this function (I really hope)
        val remainingSize = AtomicLong(size!!)
        val scope = CoroutineScope(Dispatchers.IO)
        // I think this'll work well (maybe it does not)
        val semaphore = Semaphore(jobs)

        while (remainingSize.get() > 0) {
            if (semaphore.availablePermits > 0) {
                // acquire before the loop, so we don't spawn more coroutines while the newly created one is booting up
                // (I believe this happened before)
                semaphore.acquire()
                scope.launch {
                    try {
                        val file = createFile()
                        if (file.exists()) {
                            val size = file.length()
                            val rem = remainingSize.addAndGet(-size)
                            log.info("Created file", "size" to size, "remaining" to rem)
                        }
                    } catch (e: Exception) {
                        log.error(e)
                    } finally {
                        log.info("Done.")
                        delay(500L)
                        semaphore.release()
                    }
                }
            } else {
                // do not busy-wait, we have things to do
                delay(10L)
            }
        }

    }

    /**
     * Create a single file.
     *
     * @return The absolute filename of the created file
     */
    private suspend fun createFile(): File {
        val extension = AbstractScrambler.randomExtension()
        val filename = "${RandomStringUtils.secure().nextAlphanumeric(32)}.$extension"
        // SAFETY: extension is coming from AbstractScrambler, so it better exists in the mapping.
        val scrambler = AbstractScrambler.scramblerExtensionMap[extension]!!

        val file = scrambler.createNewFile(filename, targetDirectory)

        return file
    }
}