package de.lschnartzke.fscramble.scramblers

import com.itextpdf.io.font.FontNames
import com.itextpdf.io.image.ImageData
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.font.PdfFont
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.font.PdfTrueTypeFont
import com.itextpdf.kernel.geom.Rectangle
import com.itextpdf.kernel.pdf.*
import com.itextpdf.layout.Document
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.itextpdf.layout.Canvas
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import de.lschnartzke.fscramble.cache.DataCache
import io.klogging.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import kotlin.math.absoluteValue

/**
 * Class responsible for scrambling PDF files.
 *
 * Instances of this class are passed PDF files which they scramble according to their configuration.
 * This means that text will be added or removed, pictures might be inserted and the layout may be changed.
 * Afterward the file is written back, replacing the original file (unless specified otherwise)
 */
class PdfScrambler() : AbstractScrambler() {
    // List of images that may be inserted into the pdf
    private val logger =  logger<PdfScrambler>()

    /**
     * Determine where to write the resulting PDF file
     *
     * @param ifile input file. MUST point to a PDF file
     * @param ofile output file. MAY point to a file or directory. If ofile is a directory, the writer will try to
     * write to `ofile/ifile`
     */
    private fun pdfWriter(ifile: String, ofile: String): PdfWriter {
        val outfile = getOutfile(ifile, ofile)
        val writer = PdfWriter(outfile)

        return writer
    }

    override suspend fun createNewFile(filename: String, outpath: String, scrambleCount: Int): File {
        val outfile = getOutfile(filename, outpath)
        val writer = PdfWriter(outfile)
        val doc = Document(PdfDocument(writer))

        doScramble(scrambleCount, doc)

        doc.close()
        return outfile
    }

    override suspend fun scramble(input: String, output: String, scrambleCount: Int) = withContext(Dispatchers.IO) {
        val reader = PdfReader(input)
        val writer = pdfWriter(input, output)

        val pdfDoc = Document(PdfDocument(reader, writer))

        doScramble(scrambleCount, pdfDoc)

        pdfDoc.close()
    }

    private suspend fun doScramble(scrambleCount: Int, pdfDoc: Document) {
        repeat(scrambleCount) {
            val action = getScrambleAction()
            logger.debug("action" to action.toString())
            try {
                when (action) {
                    ScrambleAction.ADD_TEXT -> scrambleAddText(pdfDoc)
                    ScrambleAction.REMOVE_TEXT -> scrambleRemovePage(pdfDoc) // Believe it or not, this is what removing text looks like
                    ScrambleAction.ADD_MEDIA -> scrambleAddMedia(pdfDoc)
                    ScrambleAction.REMOVE_MEDIA -> scrambleRemoveMedia(pdfDoc)
                }
            } catch (e: Exception) {
                logger.error(mapOf("action" to action.toString(), "error" to e))
            }
        }
    }

    /**
     * Return a random page within the document, or null, if the document is empty
     */
    private fun getRandomPage(doc: Document): PdfPage? {
        if (doc.pdfDocument.numberOfPages == 0) return null

        val pageIndex = rng.nextInt(until = doc.pdfDocument.numberOfPages)
        return doc.pdfDocument.getPage(pageIndex)
    }

    /**
     * Return either a random paragraph from `textParagraphs` or, if that's empty, a randomly generated string
     */
    private fun getRandomParagraph(): String = DataCache.getDataCache().getRandomParagraph()

    /**
     * Add a new (empty) page somewhere in the document
     */
    private fun scrambleAddPage(doc: Document): PdfPage {
        // if the document is empty, the random index causes OutOfBounds exceptions.
        val page = if (doc.pdfDocument.numberOfPages <= 0) {
            doc.pdfDocument.addNewPage()
        } else {
            // apparently this is lua now, as the pages are indexed at one and somewhere in the callstack is a
            // --index to get the actual array index. Yay.
            val newPageIndex = rng.nextInt(until = doc.pdfDocument.numberOfPages) + 1
            doc.pdfDocument.addNewPage(newPageIndex)
        }

        return page
    }

    private fun scrambleRemovePage(doc: Document) {
        if (doc.pdfDocument.numberOfPages == 0)
            return

        val pageIndex = rng.nextInt(until = doc.pdfDocument.numberOfPages + 1)
        doc.pdfDocument.removePage(pageIndex)
    }

    private fun scrambleAddText(doc: Document) {
        val page = scrambleAddPage(doc)
        val text = getRandomParagraph()

        val canvas = Canvas(PdfCanvas(page), doc.getPageEffectiveArea(doc.pdfDocument.defaultPageSize))
        canvas.pdfCanvas.apply {
            beginText()
            setFontAndSize(doc.pdfDocument.defaultFont, 12f)
            showText(text)
            endText()
        }
        canvas.close()
    }


    private fun scrambleAddMedia(doc: Document) {
        val imageBytes = DataCache.getDataCache().getRandomImageData()?.content ?: return
        val page = scrambleAddPage(doc)
        val pdfCanvas = PdfCanvas(page)
        val canvas = Canvas(pdfCanvas, doc.getPageEffectiveArea(doc.pdfDocument.defaultPageSize))

        val data = ImageDataFactory.create(imageBytes)
        val image = Image(data)
        canvas.add(image)
        canvas.close()
    }

    private fun scrambleRemoveMedia(doc: Document) {
        // TODO: Maybe we just remove another page?
    }
}