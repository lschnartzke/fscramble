package de.lschnartzke.fscramble.scramblers

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
abstract class AbstractScrambler(val dataDirectory: String) {
    enum class ScrambleAction {
        ADD_TEXT,
        REMOVE_TEXT,
        ADD_MEDIA,
        REMOVE_MEDIA
    }
    private val actions = ScrambleAction.entries.toTypedArray()
    val rng: Random = Random(System.currentTimeMillis()) // good enough

    /**
     * Initialize the scrambler. This function will be called only once per instance. Its purpose is to initialize the
     * scrambler and cache the data it may use during scrambling (e.g. preloading images and text).
     *
     * The function may assume that only one thread is using and does not require internal locks
     */
    abstract suspend fun init()

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