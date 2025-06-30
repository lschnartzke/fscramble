package de.lschnartzke.fscramble.scramblers

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.compress.utils.IOUtils
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.zip.ZipFile

class ZipScrambler : AbstractArchiveScrambler() {
    /**
     * Create a new archive and add some (also randomly created) files to it:
     */
    override suspend fun createNewArchive(
        filename: String,
        outputDirectory: File,
        workingDirectory: File,
        scrambleCount: Int
    ): File {
        val file = File(outputDirectory, filename)
        val zipFile = ZipArchiveOutputStream(file)

        doScramble(null, zipFile, workingDirectory, scrambleCount)

        zipFile.close()
        return file
    }

    /**
     * Perform the scramble actions on the provided input file, writing the result to the output.
     * `output` is expected to be a new file, containing nothing (perhaps not even existing on disk yet).
     */
    private suspend fun doScramble(input: ZipFile?, output: ZipArchiveOutputStream, inputDirectory: File, scrambleCount: Int) {
        if (!inputDirectory.isDirectory)
            throw IllegalArgumentException("input directory is not a directory")
        val inputFiles = inputDirectory.listFiles().toMutableList()
        // cannot scramble when there are no input files and there's no input file to remove anything from
        if (inputFiles.isEmpty() && input == null)
            return

        val entryList = input?.entries()?.toList()?.toMutableList()

        repeat(scrambleCount) {
            val scrambleAction = getScrambleAction()
            if (input == null && inputFiles.isNotEmpty()) {
                // randomly add files from the input directory
                when (scrambleAction) {
                    // purely speculating, but this should add about 1/4 of all files in the input directory to the
                    // archive (citation needed though). This might not be ideal as, when creating enough archives,
                    // the amount of files stored twice or more in different archives would be quite high, which
                    // I would consider to be unrealistic unless you really want to waste space or really don't know
                    // how to properly back up your data. However, for now I am too lazy to put further work into
                    // this, so let's just call this a *temporary* workaround.
                    ScrambleAction.ADD_TEXT -> {
                        val file = inputFiles.random()
                        val entry = ZipArchiveEntry(file, file.name)
                        output.putArchiveEntry(entry)
                        FileUtils.copyFile(file, output)
                        output.closeArchiveEntry()

                        // remove to avoid duplicate files
                        inputFiles.remove(file)
                    }
                    else -> {} // nothing to do
                }
            } else {
                when (scrambleAction) {
                    // Add a new file from the input directory
                    ScrambleAction.ADD_TEXT -> {
                        if (inputFiles.isNotEmpty()) {
                            val file = inputFiles.random()
                            val entry = ZipArchiveEntry(file, file.name)
                            output.putArchiveEntry(entry)

                            // remove to avoid duplicates
                            inputFiles.remove(file)
                        }
                    }

                    // *keep* a file in the archive.
                    ScrambleAction.ADD_MEDIA -> {
                        // SAFETY: (proof) the list is guaranteed to be non-null if input is non-null, and we're only
                        // in this branch, if input is non-null. Therefore, entryList cannot be null. [ ]
                        if (entryList!!.isNotEmpty()) {
                            // SAFETY: (proof) the list is guaranteed to be non-null if input is non-null, and we're only
                            // in this branch, if input is non-null. Therefore, entryList cannot be null. [ ]
                            val entry = entryList!!.random()
                            input.getInputStream(entry).use { istream ->
                                // this will probably crash if we're reading files (a lot) bigger than available memory
                                // swap, so for safety reasons copying files that would crash this implementation are hereby
                                // discouraged and officially unsupported (until I need this feature)
                                output.write(istream.readAllBytes())
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    /**
     * Randomly remove and add (some) files to the archive
     */
    override suspend fun scramble(input: String, output: String, scrambleCount: Int) {
        val ifile  = File(input)
        val ofile = getOutfile(input, output)
        val workingDirectory = ifile.parentFile
        val inputZipFile = ZipFile(ifile)
        val outputZipFile = ZipArchiveOutputStream(ofile)

        doScramble(inputZipFile, outputZipFile, workingDirectory, scrambleCount)
        inputZipFile.close()
        outputZipFile.close()
    }
}