package de.lschnartzke.fscramble.scramblers

import de.lschnartzke.fscramble.cache.DataCache
import io.klogging.logger
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.File

class DocxScrambler : AbstractScrambler() {
    private val logger = logger<DocxScrambler>()

    override suspend fun scramble(input: String, output: String, scrambleCount: Int) {
        val outfile = getOutfile(input, output)
        val istream = File(input).inputStream().buffered()
        val doc = XWPFDocument(istream)

        repeat(scrambleCount) {
            val action = getScrambleAction()
            logger.info("action" to action.toString())

            when (action) {
                ScrambleAction.ADD_TEXT -> scrambleAddText(doc)
                ScrambleAction.REMOVE_TEXT -> scrambleRemoveText(doc)
                ScrambleAction.ADD_MEDIA -> scrambleAddMedia(doc)
                ScrambleAction.REMOVE_MEDIA -> scrambleRemoveMedia(doc)
            }
        }


        doc.write(outfile.outputStream())
    }

    private suspend fun scrambleAddText(doc: XWPFDocument) {
        val text = DataCache.getDataCache().getRandomParagraph()
        val paragraph = doc.createParagraph()
        paragraph.createRun().apply {
            setText(text)
            paragraph.addRun(this)
        }

    }

    private suspend fun scrambleRemoveText(doc: XWPFDocument) {

    }

    private suspend fun scrambleAddMedia(doc: XWPFDocument) {
        val data = DataCache.getDataCache().getRandomImageData() ?: return
        val 

    }

    private suspend fun scrambleRemoveMedia(doc: XWPFDocument) {

    }
}