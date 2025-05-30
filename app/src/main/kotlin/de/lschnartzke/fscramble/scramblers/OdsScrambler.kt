package de.lschnartzke.fscramble.scramblers

import de.lschnartzke.fscramble.cache.DataCache
import io.klogging.logger
import org.odftoolkit.odfdom.doc.OdfDocument
import org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument
import org.odftoolkit.odfdom.doc.table.OdfTableCell
import java.io.File

class OdsScrambler : AbstractScrambler() {
    private val logger = logger<OdsScrambler>()

    override suspend fun createNewFile(filename: String, outpath: String, scrambleCount: Int): File {
        val outfile = getOutfile(filename, outpath)
        val doc = OdfSpreadsheetDocument.newSpreadsheetDocument()

        doScramble(scrambleCount, doc)

        doc.save(outfile)
        return outfile
    }

    override suspend fun scramble(input: String, output: String, scrambleCount: Int) {
        val outfile = getOutfile(input, output)
        val doc = OdfSpreadsheetDocument.loadDocument(input)

        doScramble(scrambleCount, doc)

        doc.save(outfile)
    }

    private suspend fun doScramble(scrambleCount: Int, doc: OdfSpreadsheetDocument) {
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
    }

    private suspend fun getRandomCell(doc: OdfSpreadsheetDocument): OdfTableCell? {
        val table = doc.getTableList(true)[0] ?: return null
        val columns = table.columnList
        if (columns.isEmpty())
            return null
        val row = columns[rng.nextInt(until = columns.size)]
        if (row.cellCount == 0)
            return null

        return row.getCellByIndex(rng.nextInt(until = row.cellCount))
    }

    private suspend fun scrambleAddText(doc: OdfSpreadsheetDocument) {
        val cell = getRandomCell(doc) ?: return

        cell.displayText = DataCache.getDataCache().getRandomParagraph()
    }

    private suspend fun scrambleRemoveText(doc: OdfSpreadsheetDocument) {
        val cell = getRandomCell(doc) ?: return
        cell.displayText = ""
    }

    private suspend fun scrambleAddMedia(doc: OdfSpreadsheetDocument) {
        val imageData = DataCache.getDataCache().getRandomImageData() ?: return
        val cell = getRandomCell(doc) ?: return
        val mediaUri = doc.newImage(imageData.file.toURI())

        cell.stringValue = mediaUri
    }

    private suspend fun scrambleRemoveMedia(doc: OdfSpreadsheetDocument) {
        // TODO: How?
    }
}