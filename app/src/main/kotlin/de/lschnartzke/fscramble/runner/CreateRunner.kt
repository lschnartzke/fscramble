package de.lschnartzke.fscramble.runner

import de.lschnartzke.fscramble.cache.DataCache
import de.lschnartzke.fscramble.config.RunConfig
import de.lschnartzke.fscramble.scramblers.AbstractArchiveScrambler
import de.lschnartzke.fscramble.scramblers.AbstractScrambler
import io.klogging.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import org.apache.commons.lang3.RandomStringUtils
import java.io.File
import java.util.concurrent.atomic.AtomicLong
import kotlin.properties.Delegates

class CreateRunner(private val config: RunConfig.Create) : AbstractRunner() {
    private val log = logger<CreateRunner>()
    private val targetDirectory: String = config.targetDirectory
    private val inputDirectory = config.inputDirectory ?: targetDirectory
    private var size: Long? = config.size?.size
    private var count: Long? = config.count
    private var fileTypes: List<String> = config.fileTypes
    private var jobs = config.jobs

    override fun run() = runBlocking {
        DataCache.init(config.dataDirectory)
        AbstractScrambler.overrideScrambleActions(AbstractScrambler.ScrambleAction.ADD_MEDIA, AbstractScrambler.ScrambleAction.ADD_TEXT)

        // TODO: Currently, setGeneratedFileTypes overrides the list of extensions while enableArchives adds to it.
        // thus, enableArchives needs to be called after setGeneratedFileTypes, otherwise the archives will be overridden
        AbstractScrambler.setGeneratedFileTypes(fileTypes)
        AbstractScrambler.enableArchives(config.archiveTypes)

        val tdir = File(targetDirectory)
        if (!tdir.exists()) {
            tdir.mkdirs()
        }

        if (count == null) {
            createFilesUsingSize()
        } else {
            createFilesUsingCount()
        }
    }

    private suspend fun createFilesUsingCount() {
        // SAFETY: Can't be null, we've checked before calling this function (I think)
        var remaining = count!!

        val jobs = mutableListOf<Job>()
        val scope = CoroutineScope(Dispatchers.IO)
        // TODO: Find a useful way to integrate the new ratio into creating files
        while (remaining-- > 0) {
            jobs.add(scope.launch {
                createFile()
            })
        }

        for (job in jobs) {
            job.join()
        }
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
                            log.info("Created file. size: {size}, remaining : {remaining}", "size" to size, "remaining" to rem)
                        }
                    } catch (e: Exception) {
                        log.error("Failed to create file")
                    } finally {
                        semaphore.release()
                    }
                }
            } else {
                // do not busy-wait, we have things to do
                delay(10L)
            }
        }

        // weirdest way to wait for all jobs to complete
        while (semaphore.availablePermits != jobs) {
            delay(10L)
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

        try {
            val file = if (scrambler is AbstractArchiveScrambler) scrambler.createNewArchive(filename, File(targetDirectory), File(inputDirectory), 50) else scrambler.createNewFile(filename, targetDirectory)
            return file
        } catch (e: Exception) {
            log.error("Failed to create file {filename}: {error}, {stacktrace}", filename, "error" to e)
            e.printStackTrace()
            throw e
        }
    }
}