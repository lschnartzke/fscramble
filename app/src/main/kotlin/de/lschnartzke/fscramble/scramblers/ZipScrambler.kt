package de.lschnartzke.fscramble.scramblers

import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class ZipScrambler : AbstractScrambler(){
    /**
     * Create a new archive and add some (also randomly created) files to it:
     */
    override suspend fun createNewFile(filename: String, outpath: String, scrambleCount: Int): File {
        val file = File(filename)
        val zipFile = ZipFile(file)

        

        zipFile.close()
        return file
    }

    /**
     * Randomly remove and add (some) files to the archive
     */
    override suspend fun scramble(input: String, output: String, scrambleCount: Int) {
        TODO("Not yet implemented")
    }
}