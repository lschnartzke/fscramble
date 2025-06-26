package de.lschnartzke.fscramble.scramblers

import java.io.File

/**
 * Base class for creating and scrambling tar files
 */
class TarScrambler : AbstractArchiveScrambler() {
    /**
     * Create a new archive and randomly add files to it.
     */
    override suspend fun createNewFile(filename: String, outpath: String, scrambleCount: Int): File {
        val file = File(filename)

        return file
    }

    /**
     * Randomly add and remove (some) files from the archive.
     */
    override suspend fun scramble(input: String, output: String, scrambleCount: Int) {
    }
}