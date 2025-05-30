package de.lschnartzke.fscramble.scramblers

import java.io.File
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
    companion object {
        private var actions = ScrambleAction.entries.toTypedArray()
        fun overrideScrambleActions(vararg actions: ScrambleAction) {
            AbstractScrambler.actions = actions as Array<ScrambleAction>
        }

        val extensions = arrayOf("odt", "pdf", "ods", "docx", "xlsx", "txt")

        fun randomExtension() = extensions.random()

        val scramblerExtensionMap: HashMap<String, AbstractScrambler> = hashMapOf(
            "odt" to OdfScrambler(),
            "pdf" to PdfScrambler(),
            "ods" to OdsScrambler(),
            "docx" to DocxScrambler(),
            "txt" to PlaintextScrambler(),
            "xlsx" to XlsxScrambler(),
        )
    }

    enum class ScrambleAction {
        ADD_TEXT,
        REMOVE_TEXT,
        ADD_MEDIA,
        REMOVE_MEDIA
    }

    val rng: Random = Random(System.currentTimeMillis()) // good enough


    /**
     * Create a new file from scratch, filling it with random content from the data cache. SHOULD not perform delete
     * operation (there is nothing to delete in a newly created file)
     */
    abstract suspend fun createNewFile(filename: String, outpath: String, scrambleCount: Int = 50): File

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