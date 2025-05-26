package de.lschnartzke.fscramble.scramblers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import kotlin.random.Random

/**
 * Base class for all scramblers.
 *
 * The scramblers are provided with a data directory, which cotains files that may be used during scrambling
 * (e.g. adding image files to documents or PDFs).
 *
 * The structure of the dataDirectory is expected to be as follows:
 *
 * dataDirectory
 * |
 * - file type
 *   |
 *   - data file
 *
 *   so, for example, to provide data specifically to be used for PdfScrambler:
 *   `dataDirectory/pdf/{file}
 */
abstract class AbstractScrambler() {
    enum class ScrambleAction {
        ADD_TEXT,
        REMOVE_TEXT,
        ADD_MEDIA,
        REMOVE_MEDIA
    }
    private var actions = ScrambleAction.entries.toTypedArray()
    val rng: Random = Random(System.currentTimeMillis()) // good enough

    fun overrideScrambleActions(vararg actions: ScrambleAction) {
        this.actions = actions as Array<ScrambleAction>
    }

    protected fun getOutfile(input: String, output: String): File {
        val ofile = File(output)
        val ifile = File(input)

        return if (ofile.isDirectory) {
            File(ofile, ifile.name)
        } else {
            ofile
        }
    }

    /**
     * Scramble the provided file.
     * @param input - the file to scramble. Must always point to a file
     * @param output - Where to save the scrambled file. Can be a filename or existing directory.
     *                 If output points to a directory, the resulting file will be at output/input.pdf
     */
    abstract suspend fun scramble(input: String, output: String, scrambleCount: Int)

    /**
     * Returns a random action that shall be performed on the open file. The exact action performed will depend on
     * the file being scrambled.
     */
    protected fun getScrambleAction(): ScrambleAction {
        return actions[rng.nextInt(until = actions.size)]
    }
}