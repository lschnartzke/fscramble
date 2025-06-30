package de.lschnartzke.fscramble.scramblers

import java.io.File

/**
 * Base class for randomly scrambling and creating archives.
 */
abstract class AbstractArchiveScrambler : AbstractScrambler() {
    override suspend fun createNewFile(filename: String, outpath: String, scrambleCount: Int): File {
        throw IllegalCallerException("when using archive scramblers, createNewArchive(...) must be called instead of createNewFile(...)")
    }

    /**
     * Create a new archive from scratch. Takes the file name and its working directory, i.e. the directory
     * containing files that can be included in the archive.
     *
     * @param filename name of the archive, including extension. Must not contain any path information
     * @param outputDirectory the directory in which to store the resulting file.
     * @param workingDirectory the directory containing files that can be put in the archive
     * @param scrambleCount how many scramble actions to perform on the file. Will randomly add files, but not remove any.
     */
    abstract suspend fun createNewArchive(filename: String, outputDirectory: File, workingDirectory: File,  scrambleCount: Int): File

}