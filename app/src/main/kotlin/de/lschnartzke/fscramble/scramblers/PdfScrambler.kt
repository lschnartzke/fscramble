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
class PdfScrambler(dataDirectory: String) : AbstractScrambler(dataDirectory) {
    // List of images that may be inserted into the pdf
    val imageData: MutableList<ImageData> = mutableListOf()
    private val logger =  logger<PdfScrambler>()

    /**
     * Determine where to write the resulting PDF file
     *
     * @param ifile input file. MUST point to a PDF file
     * @param ofile output file. MAY point to a file or directory. If ofile is a directory, the writer will try to
     * write to `ofile/ifile`
     */
    private fun pdfWriter(ifile: String, ofile: String): PdfWriter {
        val outfile = File(ofile)
        val writer: PdfWriter = if (outfile.isDirectory) {
            PdfWriter(File(ofile, ifile))
        } else {
            PdfWriter(ofile)
        }

        return writer
    }


    private suspend fun loadImageFile(file: File) = withContext(Dispatchers.IO) {
        val data = ImageDataFactory.create(file.path)
        imageData.add(data)
    }

    override suspend fun init() {
        val pdfDataDirectory = File(dataDirectory, "pdf")
        if (!pdfDataDirectory.isDirectory) {
            return
        }

        val files = pdfDataDirectory.listFiles() ?: return
        for (file in files) {
            when (file.extension) {
                "txt" -> loadTextFile(file)
                "png", "gif", "jpg", "jpeg", "bmp", "svg" -> loadImageFile(file)
                else -> {} // nothing to do
            }
        }
    }

    override suspend fun scramble(input: String, output: String, scrambleCount: Int) = withContext(Dispatchers.IO) {
        val reader = PdfReader(input)
        val writer = pdfWriter(input, output)

        val pdfDoc = Document(PdfDocument(reader, writer))

        repeat(scrambleCount) {
            val action = getScrambleAction()
            when (action) {
                ScrambleAction.ADD_TEXT -> scrambleAddText(pdfDoc)
                ScrambleAction.REMOVE_TEXT -> scrambleRemovePage(pdfDoc) // Believe it or not, this is what removing text looks like
                ScrambleAction.ADD_MEDIA -> scrambleAddMedia(pdfDoc)
                ScrambleAction.REMOVE_MEDIA -> scrambleRemoveMedia(pdfDoc)
            }
        }

        pdfDoc.close()
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
    private fun getRandomParagraph(): String =
        if (textParagraphs.isEmpty()) {
            // TODO: Generate random string
            ""
        } else {
            textParagraphs[rng.nextInt(until = textParagraphs.size)]
        }


    /**
     * Add a new (empty) page somewhere in the document
     */
    private fun scrambleAddPage(doc: Document): PdfPage {
        val newPageIndex = rng.nextInt(until = doc.pdfDocument.numberOfPages).absoluteValue
        return doc.pdfDocument.addNewPage(newPageIndex)
    }

    private fun scrambleRemovePage(doc: Document) {
        println("remove page")

        if (doc.pdfDocument.numberOfPages == 0)
            return

        val pageIndex = rng.nextInt(until = doc.pdfDocument.numberOfPages)
        doc.pdfDocument.removePage(pageIndex)
    }

    private fun scrambleAddText(doc: Document) {
        println("add text")
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
        println("add media")
        if (imageData.isEmpty())
            return // nothing to do
        val page = scrambleAddPage(doc)
        val pdfCanvas = PdfCanvas(page)
        val canvas = Canvas(pdfCanvas, doc.getPageEffectiveArea(doc.pdfDocument.defaultPageSize))

        val data = imageData[rng.nextInt(until = imageData.size)]
        val image = Image(data)
        canvas.add(image)
        canvas.close()
    }

    private fun scrambleRemoveMedia(doc: Document) {
        // TODO: Maybe we just remove another page?
        println("remove media")
    }
}