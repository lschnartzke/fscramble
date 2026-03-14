package de.lschnartzke.fscramble.scramblers

import de.lschnartzke.fscramble.cache.DataCache
import io.klogging.logger
import kotlinx.coroutines.runBlocking
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File

class XlsxScrambler : AbstractScrambler() {
    private val logger = logger<XlsxScrambler>()

    override  fun scramble(input: String, output: String, scrambleCount: Int) {
        val outfile = getOutfile(input, output)
        val doc = XSSFWorkbook(input)

        doScramble(scrambleCount, doc)

        doc.write(outfile.outputStream())
        doc.close()
    }

    private  fun doScramble(scrambleCount: Int, doc: XSSFWorkbook) {
        repeat(scrambleCount) {
            val action = getScrambleAction()
            runBlocking { logger.debug("action" to action.toString()) }
            when (action) {
                ScrambleAction.ADD_TEXT -> scrambleAddText(doc)
                ScrambleAction.REMOVE_TEXT -> scrambleRemoveText(doc)
                ScrambleAction.ADD_MEDIA -> scrambleAddMedia(doc)
                ScrambleAction.REMOVE_MEDIA -> scrambleRemoveMedia(doc)
            }
        }
    }

    override  fun createNewFile(filename: String, outpath: String, scrambleCount: Int): File {
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
            // this is probably not what you'd want, but it's working for the time being (might fix later, idk)
            sheet.getRow(rng.nextInt(from = 0, until = sheet.lastRowNum)) ?: sheet.createRow(lastCreatedRowIndex++)
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

    private  fun scrambleAddText(doc: XSSFWorkbook) {
        // SAFETY: unless create == false this will never be null
        val sheet = getOrCreateRandomSheet(doc)!!
        val cell = getRandomCellFromSheet(sheet)
        cell.setCellValue(DataCache.getDataCache().getRandomParagraph())
    }

    private  fun scrambleRemoveText(doc: XSSFWorkbook) {
        val sheet = getOrCreateRandomSheet(doc, create = false) ?: return
        val cell = getRandomCellFromSheet(sheet)
        cell.setBlank()
    }

    private  fun scrambleAddMedia(doc: XSSFWorkbook) {
        val imageData = DataCache.getDataCache().getRandomImageData() ?: return
        // Safety: Unless create == false this cannot be null
        val sheet = getOrCreateRandomSheet(doc)!!

        val pictureIndex = doc.addPicture(imageData.content, imageData.xlsxPictureType())
        val pictureData = doc.allPictures[pictureIndex]
    }

    private  fun scrambleRemoveMedia(doc: XSSFWorkbook) {
        if (doc.allPictures.isEmpty())
            return

        val pictureIndex = rng.nextInt(until = doc.allPictures.size)
        val pictureData = doc.allPictures[pictureIndex]
        val picturePart = pictureData.packagePart
        runBlocking { logger.info("picturePartName" to picturePart.partName) }
        doc.`package`.removePart(pictureData.packagePart)
        doc.allPictures.removeAt(pictureIndex)


    }
}