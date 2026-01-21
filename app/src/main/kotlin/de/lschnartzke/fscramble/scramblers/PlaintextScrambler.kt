package de.lschnartzke.fscramble.scramblers

import de.lschnartzke.fscramble.cache.DataCache
import io.klogging.logger
import kotlinx.coroutines.runBlocking
import java.io.File

class PlaintextScrambler : AbstractScrambler() {
    private val logger = logger<PlaintextScrambler>()

    override  fun scramble(input: String, output: String, scrambleCount: Int) {
        val outfile = getOutfile(input, output)
        val fileLines = File(input).readLines().toMutableList()

        doScramble(scrambleCount, fileLines)

        val ostream = outfile.outputStream().bufferedWriter()
        fileLines.forEach {
            ostream.write(it)
        }

        ostream.close()
    }

    private  fun doScramble(scrambleCount: Int, fileLines: MutableList<String>) {
        repeat(scrambleCount) {
            val action = getScrambleAction()
            runBlocking { logger.debug("action" to action.toString()) }
            when (action) {
                ScrambleAction.ADD_TEXT, ScrambleAction.ADD_MEDIA -> scrambleAddText(fileLines)
                ScrambleAction.REMOVE_TEXT, ScrambleAction.REMOVE_MEDIA -> scrambleRemoveText(fileLines)
            }
        }
    }


    override  fun createNewFile(filename: String, outpath: String, scrambleCount: Int): File {
        val outfile = getOutfile(filename, outpath)

        val lines = mutableListOf<String>()
        doScramble(scrambleCount, lines)

        val stream = outfile.outputStream().bufferedWriter()
        lines.forEach {
            stream.write(it)
        }

        return outfile
    }

    private  fun scrambleAddText(lines: MutableList<String>) {
        val line = DataCache.getDataCache().getRandomParagraph()
        if (lines.isEmpty()) {
            lines.add(line)
        } else {
            lines.add(rng.nextInt(until = lines.size), line)
        }
    }

    private  fun scrambleRemoveText(lines: MutableList<String>) {
        if (lines.isNotEmpty())
            lines.removeAt(rng.nextInt(until = lines.size))
    }
}