package de.lschnartzke.fscramble.scramblers

import de.lschnartzke.fscramble.wrapper.ArchiveEntryWrapper
import de.lschnartzke.fscramble.wrapper.ArchiveWrapper
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.io.FileUtils
import java.io.File
import kotlin.io.path.absolute

class ArchiveScrambler : AbstractArchiveScrambler() {
    override  fun createNewArchive(
        filename: String,
        outputDirectory: File,
        workingDirectory: File,
        scrambleCount: Int
    ): File {
        val file = File(outputDirectory, filename)
        val archiveFile = ArchiveWrapper.getArchiveWrapperByFile(file)

        doScramble(archiveFile, workingDirectory, scrambleCount)

        archiveFile.close()
        return file
    }

    fun doScramble(archive: ArchiveWrapper, inputDirectory: File, scrambleCount: Int) {
        if (!inputDirectory.isDirectory)
            throw IllegalArgumentException("input directory must be a directory")

        val inputFiles = inputDirectory.listFiles().toMutableList()
        // cannot scramble when there are no input files and there's no input file to remove from
        if (inputFiles.isEmpty() && archive.hasNoInputFile())
            return

        val archiveEntries = archive.entries

        fun addFileToArchive() {
            val file = inputFiles.random()
            val entry = archive.getArchiveEntryWrapperForFile(file)
            archive.putArchiveEntry(entry)
            if (file.isFile)
                FileUtils.copyFile(file, archive)
            archive.closeArchiveEntry()
            if (file.isDirectory)
                archive.addDirectory(inputDirectory.toPath().absolute().normalize(), file.toPath().absolute().normalize())

            inputFiles.remove(file)
        }

        repeat(scrambleCount) {
            val scrambleAction = getScrambleAction()
            if (archive.hasNoInputFile() && inputFiles.isNotEmpty()) {
                when (scrambleAction) {
                    ScrambleAction.ADD_TEXT -> {
                        addFileToArchive()
                    }
                    else -> {}
                }
            } else {
                when (scrambleAction) {
                    ScrambleAction.ADD_TEXT -> {
                        if (inputFiles.isNotEmpty()) {
                            addFileToArchive()
                        }
                    }

                    // *keep* file in the archive
                    ScrambleAction.ADD_MEDIA -> {
                        if (archiveEntries.isNotEmpty()) {
                            val entry = archiveEntries.random()
                            archive.putArchiveEntry(entry)
                            if (entry.isDirectory) {
                                archive.getInputStream(entry).use {
                                    // this will probably crash if we're reading files (a lot) bigger than available memory
                                    // swap, so for safety reasons copying files that would crash this implementation are hereby
                                    // discouraged and officially unsupported (until I need this feature)
                                    archive.write(it.readAllBytes())
                                }
                            }
                            archive.closeArchiveEntry()
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    override  fun scramble(input: String, output: String, scrambleCount: Int) {
        val ifile = File(input)
        val ofile = getOutfile(input, output)
        val workingDirectory = ifile.parentFile
        val archiveWrapper = ArchiveWrapper.getArchiveWrapperByFile(ifile, ofile)

        doScramble(archiveWrapper, workingDirectory, scrambleCount)

        archiveWrapper.close()
    }
}