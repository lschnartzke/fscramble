package de.lschnartzke.fscramble.scramblers

import de.lschnartzke.fscramble.cache.DataCache
import io.klogging.logger
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File

class XlsxScrambler : AbstractScrambler() {
    private val logger = logger<XlsxScrambler>()

    override suspend fun scramble(input: String, output: String, scrambleCount: Int) {
        val outfile = getOutfile(input, output)
        val doc = XSSFWorkbook(input)

        doScramble(scrambleCount, doc)

        doc.write(outfile.outputStream())
        doc.close()
    }

    private suspend fun doScramble(scrambleCount: Int, doc: XSSFWorkbook) {
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

    override suspend fun createNewFile(filename: String, outpath: String, scrambleCount: Int): File {
        val outfile = getOutfile(filename, outpath)
        val doc = XSSFWorkbook()

        doScramble(scrambleCount, doc)

        doc.write(outfile.outputStream())
        doc.close()
        return outfile
    }

    private var lastCreatedRowIndex = 0
    private var lastCreatedColumnIndex = 0
    private fun getRandomCellFromSheet(sheet: XSSFSheet): XSSFCell {
        val row = if (sheet.firstRowNum <= 0 || sheet.lastRowNum <= 0) {
            sheet.createRow(lastCreatedRowIndex++)
        } else {
            sheet.getRow(rng.nextInt(from = sheet.firstRowNum, until = sheet.lastRowNum))
        }
        val cell = if (row.firstCellNum <= 0 || row.lastCellNum <= 0) {
            row.createCell(lastCreatedColumnIndex++)
        } else  {
            row.getCell(rng.nextInt(from = row.firstCellNum.toInt(), until = row.lastCellNum.toInt()))
        }
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
        val imageData = DataCache.getDataCache().getRandomImageData() ?: return
        // Safety: Unless create == false this cannot be null
        val sheet = getOrCreateRandomSheet(doc)!!

        val pictureIndex = doc.addPicture(imageData.content, imageData.xlsxPictureType())
        val pictureData = doc.allPictures[pictureIndex]
        logger.info("picturePartName" to pictureData.packagePart.partName)
    }

    private suspend fun scrambleRemoveMedia(doc: XSSFWorkbook) {
        if (doc.allPictures.isEmpty())
            return

        val pictureIndex = rng.nextInt(until = doc.allPictures.size)
        val pictureData = doc.allPictures[pictureIndex]
        val picturePart = pictureData.packagePart
        logger.info("picturePartName" to picturePart.partName)
        doc.`package`.removePart(pictureData.packagePart)
        doc.allPictures.removeAt(pictureIndex)


    }
}