package de.lschnartzke.fscramble.scramblers

import org.odftoolkit.odfdom.doc.OdfTextDocument
import org.odftoolkit.odfdom.pkg.OdfElement
import java.io.File

class OdfScrambler(dataDirectory: String) : AbstractScrambler(dataDirectory) {
    override suspend fun init() {
        TODO("Not yet implemented")
    }

    override suspend fun scramble(input: String, output: String, scrambleCount: Int) {
        val ifile = File(input)
        val outfile = getOutfile(input, output)
        when (ifile.extension) {
            "odt" -> scrambleTextDocument(ifile, outfile, scrambleCount)
        }
    }

    private fun scrambleTextDocument(input: File, output: File, scrambleCount: Int) {
        val document = OdfTextDocument.loadDocument(input)

        repeat(scrambleCount) {
            val action = getScrambleAction()
            when (action) {
                ScrambleAction.ADD_TEXT -> scrambleAddText(document)
                ScrambleAction.REMOVE_TEXT -> scrambleRemoveText(document)
                ScrambleAction.ADD_MEDIA -> scrambleAddMedia(document)
                ScrambleAction.REMOVE_MEDIA -> scrambleRemoveMedia(document)
            }
        }
    }

    /**
     * Insert new text at a random place in the document
     */
    private fun scrambleAddText(doc: OdfTextDocument) {
        val root = doc.contentRoot
    }

    /**
     * Remove random text from the document
     */
    private fun scrambleRemoveText(doc: OdfTextDocument) {

    }

    /**
     * Add media at a random location in the document
     */
    private fun scrambleAddMedia(doc: OdfTextDocument) {

    }

    /**
     * Randomly remove one media Element from the document
     */
    private fun scrambleRemoveMedia(doc: OdfTextDocument) {

    }
}