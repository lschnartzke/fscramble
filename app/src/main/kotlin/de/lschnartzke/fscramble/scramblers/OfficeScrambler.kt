package de.lschnartzke.fscramble.scramblers

import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream

class OfficeScrambler(dataDirectory: String) : AbstractScrambler(dataDirectory) {
    val mediaFiles: MutableList<String> = mutableListOf()

    suspend fun loadImageFile(file: File) {

    }

    override suspend fun init() {
        val dataDir = File(dataDirectory)
        if (!dataDir.isDirectory)
            return
        val files = dataDir.listFiles() ?: return

        for (file in files) {
            when (file.extension) {
                "txt" -> loadTextFile(file)
                "png", "jpeg", "jpg", "tiff", "gif", "svg" -> loadImageFile(file)
                else -> {} // nothing to do (yet)
            }
        }
    }

    override suspend fun scramble(input: String, output: String, scrambleCount: Int) {

    }
}