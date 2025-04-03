package de.lschnartzke.fscramble.scramblers

import com.itextpdf.io.image.ImageData
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.geom.Rectangle
import com.itextpdf.layout.Document
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfPage
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.itextpdf.layout.Canvas
import com.itextpdf.layout.element.Paragraph
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream

/**
 * Class responsible for scrambling PDF files.
 *
 * Instances of this class are passed PDF files which they scramble according to their configuration.
 * This means that text will be added or removed, pictures might be inserted and the layout may be changed.
 * Afterward the file is written back, replacing the original file (unless specified otherwise)
 */
class PdfScrambler(dataDirectory: String) : AbstractScrambler(dataDirectory) {
    // List of text paragraphs (can be multiple lines) that may be used during ADD_TEXT scrambling
    val textParagraphs: MutableList<String> = mutableListOf()

    // List of images that may be inserted into the pdf
    val imageData: MutableList<ImageData> = mutableListOf()

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

    private suspend fun loadTextFile(file: File) = withContext(Dispatchers.IO) {
        val reader = FileInputStream(file).bufferedReader()
        val builder = StringBuilder()
        for (line in reader.readLines()) {
            if (line.isEmpty() && builder.isNotEmpty()) {
                textParagraphs.add(builder.toString())
                builder.clear()
                continue
            } else if (line.isEmpty()) {
                continue
            }

            builder.append(line)
        }
    }

    private suspend fun loadImageFile(file: File) = withContext(Dispatchers.IO) {
        val data = ImageDataFactory.create(file.name)
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
    }

    /**
     * Return a random page within the document, or null, if the document is empty
     */
    private fun getRandomPage(doc: Document): PdfPage? {
        if (doc.pdfDocument.numberOfPages == 0) return null

        val pageIndex = rng.nextInt() % doc.pdfDocument.numberOfPages
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
        val newPageIndex = rng.nextInt() % doc.pdfDocument.numberOfPages
        return doc.pdfDocument.addNewPage(newPageIndex)
    }

    private fun scrambleRemovePage(doc: Document) {
        if (doc.pdfDocument.numberOfPages == 0)
            return

        val pageIndex = rng.nextInt() % doc.pdfDocument.numberOfPages
        doc.pdfDocument.removePage(pageIndex)
    }

    private fun scrambleAddText(doc: Document) {
        val page = scrambleAddPage(doc)
        val text = getRandomParagraph()

        val canvas = PdfCanvas(page)
        canvas.beginText()
        canvas.showText(text)
        canvas.endText()
    }


    private fun scrambleAddMedia(doc: Document) {
        val page = scrambleAddPage(doc)
        val pdfCanvas = PdfCanvas(page)
        val canvas = Canvas(pdfCanvas, doc.getPageEffectiveArea(doc.pdfDocument.defaultPageSize))

        val data = imageData[rng.nextInt(until = imageData.size)]

        canvas.add(imageData[rng.nextInt(until = imageData.size)])
        canvas.add


    }

    private fun scrambleRemoveMedia(doc: Document) {

    }

    private fun withRandomPage(doc: Document, fn: (PdfPage) -> Unit) {
        val page = getRandomPage(doc) ?: return

        fn(page)
    }
}