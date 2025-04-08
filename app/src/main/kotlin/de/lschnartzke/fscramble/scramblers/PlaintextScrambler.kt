package de.lschnartzke.fscramble.scramblers

import de.lschnartzke.fscramble.cache.DataCache
import io.klogging.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class PlaintextScrambler : AbstractScrambler() {
    private val logger = logger<PlaintextScrambler>()

    override suspend fun scramble(input: String, output: String, scrambleCount: Int) {
        val outfile = getOutfile(input, output)
        val fileLines = File(input).readLines().toMutableList()

        repeat (scrambleCount) {
            val action =getScrambleAction()
            logger.info("action" to action.toString())
            when (action) {
                ScrambleAction.ADD_TEXT, ScrambleAction.ADD_MEDIA -> scrambleAddText(fileLines)
                ScrambleAction.REMOVE_TEXT, ScrambleAction.REMOVE_MEDIA -> scrambleRemoveText(fileLines)
            }
        }

        val ostream = outfile.outputStream().bufferedWriter()
        fileLines.forEach {
            ostream.write(it)
        }

        ostream.close()
    }


    private suspend fun scrambleAddText(lines: MutableList<String>) {
        val line = DataCache.getDataCache().getRandomParagraph()
        if (lines.isEmpty()) {
            lines.add(line)
        } else {
            lines.add(rng.nextInt(until = lines.size), line)
        }
    }

    private suspend fun scrambleRemoveText(lines: MutableList<String>) {
        if (lines.isNotEmpty())
            lines.removeAt(rng.nextInt(until = lines.size))
    }
}