package de.lschnartzke.fscramble.scramblers

import de.lschnartzke.fscramble.cache.DataCache
import io.klogging.logger
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook

class XlsxScrambler : AbstractScrambler() {
    val logger = logger<XlsxScrambler>()

    override suspend fun scramble(input: String, output: String, scrambleCount: Int) {
        val outfile = getOutfile(input, output)
        val doc = XSSFWorkbook(input)

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

    private fun getRandomCellFromSheet(sheet: XSSFSheet): XSSFCell {
        val row = sheet.getRow(rng.nextInt(from = sheet.firstRowNum, until = sheet.lastRowNum))
        val cell = row.getCell(rng.nextInt(from = row.firstCellNum.toInt(), until = row.lastCellNum.toInt()))
        return cell
    }

    private fun getOrCreateRandomSheet(doc: XSSFWorkbook, create: Boolean = true): XSSFSheet? =
        if (doc.numberOfSheets == 0 && create) {
            doc.createSheet("Sheet1")
        } else if (doc.numberOfSheets != 0) {
            doc.getSheetAt(rng.nextInt(until = doc.numberOfSheets))
        } else {
            null
        }

    private suspend fun scrambleAddText(doc: XSSFWorkbook) {
        // SAFETY: unless create == false this will never be null
        val sheet = getOrCreateRandomSheet(doc)!!
        val cell = getRandomCellFromSheet(sheet)
        cell.setCellValue(DataCache.getDataCache().getRandomParagraph())
    }

    private suspend fun scrambleRemoveText(doc: XSSFWorkbook) {
        val sheet = getOrCreateRandomSheet(doc, create = false) ?: return
        val cell = getRandomCellFromSheet(sheet)
        cell.setBlank()
    }

    private suspend fun scrambleAddMedia(doc: XSSFWorkbook) {
        // SAFETY: unless create == false this will never be null
        val sheet = getOrCreateRandomSheet(doc)!!
        val cell = getRandomCellFromSheet(sheet)

        cell.
    }

    private suspend fun scrambleRemoveMedia(doc: XSSFWorkbook) {

    }
}