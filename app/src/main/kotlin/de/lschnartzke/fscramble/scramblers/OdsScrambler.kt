package de.lschnartzke.fscramble.scramblers

import io.klogging.logger
import org.odftoolkit.odfdom.doc.OdfDocument
import org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument
import org.odftoolkit.odfdom.doc.table.OdfTableCell

class OdsScrambler : AbstractScrambler() {
    private val logger = logger<OdsScrambler>()

    override suspend fun scramble(input: String, output: String, scrambleCount: Int) {
        val outfile = getOutfile(input, output)
        val doc = OdfSpreadsheetDocument.loadDocument(input)

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

        doc.save(outfile)
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


    }

    private suspend fun scrambleRemoveText(doc: OdfSpreadsheetDocument) {

    }

    private suspend fun scrambleAddMedia(doc: OdfSpreadsheetDocument) {

    }

    private suspend fun scrambleRemoveMedia(doc: OdfSpreadsheetDocument) {

    }
}