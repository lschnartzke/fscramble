package de.lschnartzke.fscramble.scramblers

import de.lschnartzke.fscramble.cache.DataCache
import io.klogging.logger
import org.odftoolkit.odfdom.doc.OdfTextDocument
import org.odftoolkit.odfdom.dom.element.office.OfficeTextElement
import org.odftoolkit.odfdom.dom.element.text.TextPElement
import org.odftoolkit.odfdom.pkg.OdfElement
import org.w3c.dom.Node
import java.io.File

class OdfScrambler() : AbstractScrambler() {
    private val logger = logger<OdfScrambler>()

    override suspend fun scramble(input: String, output: String, scrambleCount: Int) {
        val ifile = File(input)
        val outfile = getOutfile(input, output)
        when (ifile.extension) {
            "odt" -> scrambleTextDocument(ifile, outfile, scrambleCount)
        }
    }

    private suspend fun scrambleTextDocument(input: File, output: File, scrambleCount: Int) {
        val document = OdfTextDocument.loadDocument(input)

        repeat(scrambleCount) {
            val action = getScrambleAction()
            logger.info("action" to action.toString())
            when (action) {
                ScrambleAction.ADD_TEXT -> scrambleAddText(document)
                ScrambleAction.REMOVE_TEXT -> scrambleRemoveText(document)
                ScrambleAction.ADD_MEDIA -> scrambleAddMedia(document)
                ScrambleAction.REMOVE_MEDIA -> scrambleRemoveMedia(document)
            }
        }

        document.save(output)
    }

    private fun getRandomNode(parent: Node): OfficeTextElement {
        var node: Node = parent
        for (i in 0..5) {
            node = OdfElement.findNextChildNode(OfficeTextElement::class.java, node) ?: return node as OfficeTextElement
        }

        return node as OfficeTextElement
    }

    /**
     * Insert new text at a random place in the document
     */
    private suspend fun scrambleAddText(doc: OdfTextDocument) {
        doc.newParagraph(DataCache.getDataCache().getRandomParagraph())
    }

    /**
     * Remove random text from the document
     */
    private suspend fun scrambleRemoveText(doc: OdfTextDocument) {
        val node = getRandomNode(doc.contentRoot)
        try {
            node.removeContent()
            doc.contentRoot.removeChild(node)
        } catch (e: Exception) {
            logger.error("Failed to remove text element", "error" to e)
        }
    }

    /**
     * Add media at a random location in the document
     */
    private fun scrambleAddMedia(doc: OdfTextDocument) {
        val imageData = DataCache.getDataCache().getRandomImageData() ?: return
        doc.newImage(imageData.file.toURI())
        // The image is automatically added to the end of the document. I currently have no method of placing it
        // somewhere in the document, but for the sake of deduplication ration and the fact, that it is likely stored
        // separately, it does not matter (I hope).

    }

    /**
     * Randomly remove one media Element from the document
     */
    private fun scrambleRemoveMedia(doc: OdfTextDocument) {
        // How?
    }
}