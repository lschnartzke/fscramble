package de.lschnartzke.fscramble.scramblers

import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.archivers.tar.TarFile
import java.io.File

/**
 * Base class for creating and scrambling tar files
 */
class TarScrambler : AbstractArchiveScrambler() {
    /**
     * Create a new archive and randomly add files to it.
     */
    override suspend fun createNewArchive(
        filename: String,
        outputDirectory: File,
        workingDirectory: File,
        scrambleCount: Int
    ): File {
        val file = File(outputDirectory, filename)
        val tarFile = TarArchiveOutputStream(file.outputStream())

        doScramble(null, tarFile, workingDirectory, scrambleCount)

        tarFile.close()
        return file
    }

    private suspend fun doScramble(input: TarFile?, output: TarArchiveOutputStream, inputDirectory: File, scrambleCount: Int) {
        if (!inputDirectory.isDirectory)
            throw IllegalArgumentException("input directory must be a directory")

        val inputFiles = inputDirectory.listFiles()

        // cannot scramble when there are no input files and there's no input file to remove anything from
        if (inputFiles.isEmpty() && input == null)
            return

        val entryList = input?.entries?.toMutableList()

        repeat(scrambleCount) {
            val scrambleAction = getScrambleAction()
            if (input == null && inputFiles.isNotEmpty()) {
                when (scrambleAction) {
                    ScrambleAction.ADD_TEXT -> {

                    }
                    else -> {}
                }
            } else {
                when (scrambleAction) {
                    
                    else -> {}
                }
            }
        }
    }

    /**
     * Randomly add and remove (some) files from the archive.
     */
    override suspend fun scramble(input: String, output: String, scrambleCount: Int) {
        val ifile = File(input)
        val ofile = getOutfile(input, output)
        val workingDirectory = ifile.parentFile
        val inputTarFile = TarFile(ifile)
        val outputTarFile = TarArchiveOutputStream(ofile.outputStream())

        doScramble(inputTarFile, outputTarFile, workingDirectory, scrambleCount)
        inputTarFile.close()
        outputTarFile.close()
    }
}