package de.lschnartzke.fscramble.scramblers

import de.lschnartzke.fscramble.cache.DataCache
import io.klogging.logger
import org.apache.poi.util.Units
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.odftoolkit.odfdom.type.Length
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
        doc.close()
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
        // TODO: How?
    }

    private suspend fun scrambleAddMedia(doc: XWPFDocument) {
        val data = DataCache.getDataCache().getRandomImageData() ?: return
        val paragraph = doc.createParagraph()
        val run = paragraph.createRun()

        val input = data.file.inputStream()
        run.addPicture(input, data.docxPictureType(), data.file.name, Units.toEMU(1920.toDouble()), Units.toEMU(1080.toDouble()))
        run.addBreak()
    }

    private suspend fun scrambleRemoveMedia(doc: XWPFDocument) {
        try {
            if (doc.allPictures.isNotEmpty()) {
                doc.allPictures.removeAt(rng.nextInt(until = doc.allPictures.size))
                return
            } else if (doc.allPackagePictures.isNotEmpty()) {
                doc.allPackagePictures.removeAt(rng.nextInt(until = doc.allPackagePictures.size))
                return
            }
        } catch (e: Exception) {
            logger.error("Failed to remove media", "error" to e)
        }
    }
}