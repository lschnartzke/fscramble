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

        val supportedExtensions: List<String>
            get() = listOf("odt", "pdf", "ods", "docx", "xlsx", "txt")
        var extensions = supportedExtensions.toList()
            private set

        /**
         * Set what types of files can be scrambled and created. MUST be called before starting the scrambling/creation process.
         *
         * Ideally, the list does not contain any duplicates, as this will also affect the probability for the file
         * type to be created/scrambled. (Note that this could be fixed by using a HashSet, but I forgot about until
         * now and don't feel like vibe-refactoring this atm, might fix later idk)
         *
         * @throws IllegalArgumentException if `newExtensions` contains unsupported extensions
         */
        fun setGeneratedFileTypes(newExtensions: List<String>) {
            if (!supportedExtensions.containsAll(newExtensions))
                throw IllegalArgumentException("List contains unsupported extensions: $newExtensions (allowed: $supportedExtensions)")

            extensions = newExtensions
        }

        fun enableArchives(archiveTypes: List<String> = AbstractArchiveScrambler.supportedArchives) {
            // TODO: Too many cross references across abstract classes. This should be cleaned up (probably won't)
            if (!AbstractArchiveScrambler.supportedArchives.containsAll(archiveTypes))
                throw IllegalArgumentException("List contains unsupported archives: $archiveTypes (allowed: ${AbstractArchiveScrambler.supportedArchives})")

            extensions = extensions.toMutableList().apply { addAll(archiveTypes) }
        }

        fun randomExtension() = extensions.random()

        val scramblerExtensionMap: HashMap<String, AbstractScrambler> = hashMapOf(
            "odt" to OdfScrambler(),
            "pdf" to PdfScrambler(),
            "ods" to OdsScrambler(),
            "docx" to DocxScrambler(),
            "txt" to PlaintextScrambler(),
            "xlsx" to XlsxScrambler(),
            "tar" to ArchiveScrambler(),
            "zip" to ArchiveScrambler(),
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
     *
     * Creates a new file in `outpath`. Thus, it can be assumed that the resulting file will be at `outpath/filename`.
     *
     * @param filename The basename of te file. This includes name and extension, but no path.
     * @param outpath The directory in which to store the resulting file.
     * @param scrambleCount How many actions to perform on the new file.
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
     * @param input - the file to scramble. Must always point to a file. If the scrambling involves archives, the parent
     * of the input file should be a directory containing files. Single-file mode will cause issues with this.
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