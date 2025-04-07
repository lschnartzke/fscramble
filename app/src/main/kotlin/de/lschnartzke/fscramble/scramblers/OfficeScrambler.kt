package de.lschnartzke.fscramble.scramblers

import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream

class OfficeScrambler() : AbstractScrambler() {
    val mediaFiles: MutableList<String> = mutableListOf()

    suspend fun loadImageFile(file: File) {

    }

    override suspend fun scramble(input: String, output: String, scrambleCount: Int) {

    }
}