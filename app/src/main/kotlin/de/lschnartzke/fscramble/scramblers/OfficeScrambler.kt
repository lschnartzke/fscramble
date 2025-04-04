package de.lschnartzke.fscramble.scramblers

class OfficeScrambler(dataDirectory: String) : AbstractScrambler(dataDirectory) {
    val textParagraphs: MutableList<String> = mutableListOf()
    


    override suspend fun init() {
    }

    override suspend fun scramble(input: String, output: String, scrambleCount: Int) {

    }
}