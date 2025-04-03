package de.lschnartzke.fscramble.scramblers

import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfPage
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.element.Paragraph
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Class responsible for scrambling PDF files.
 *
 * Instances of this class are passed PDF files which they scramble according to their configuration.
 * This means that text will be added or removed, pictures might be inserted and the layout may be changed.
 * Afterward the file is written back, replacing the original file (unless specified otherwise)
 */
class PdfScrambler(dataDirectory: String) : AbstractScrambler(dataDirectory) {
    private fun pdfWriter(ifile: String, ofile: String): PdfWriter {
        val outfile = File(ofile)
        val writer: PdfWriter =  if (outfile.isDirectory) {
            PdfWriter(File(ofile, ifile))
        } else {
            PdfWriter(ofile)
        }

        return writer
    }

    override suspend fun scramble(input: String, output: String, scrambleCount: Int) = withContext(Dispatchers.IO) {
        val reader = PdfReader(input)
        val writer = pdfWriter(input, output)

        val pdfDoc = PdfDocument(reader, writer)

        repeat(scrambleCount) {
            val action = getScrambleAction()
            when(action) {
                ScrambleAction.ADD_PAGE -> scrambleAddPage(pdfDoc)
                ScrambleAction.REMOVE_PAGE -> scrambleRemovePage(pdfDoc)
                ScrambleAction.ADD_TEXT -> scrambleAddText(pdfDoc)
                ScrambleAction.REMOVE_TEXT -> scrambleRemoveText(pdfDoc)
                ScrambleAction.ADD_MEDIA -> scrambleAddMedia(pdfDoc)
                ScrambleAction.REMOVE_MEDIA -> scrambleRemoveMedia(pdfDoc)
            }
        }
    }

    /**
     * Add a new (empty) page somewhere in the document
     */
    private fun scrambleAddPage(doc: PdfDocument) {
        val newPageIndex = rng.nextInt() % doc.numberOfPages
        doc.addNewPage(newPageIndex)
    }

    private fun scrambleRemovePage(doc: PdfDocument) {
        if (doc.numberOfPages == 0)
            return

        val pageIndex = rng.nextInt() % doc.numberOfPages
        doc.removePage(pageIndex)
    }

    private fun scrambleAddText(doc: PdfDocument) {
        // TODO: Grab text from a txt file instead of random letters?
    }

    /**
     * Go to a random page, pick random paragraph(s) and remove them.
     * TODO: What if the page does not contain paragraphs? New page or just ignore?
     */
    private fun scrambleRemoveText(doc: PdfDocument) {
        if (doc.numberOfPages == 0)
            return

        val pageIndex = rng.nextInt() % doc.numberOfPages
        val page = doc.getPage(pageIndex)
    }

    private fun scrambleAddMedia(doc: PdfDocument) {

    }

    private fun scrambleRemoveMedia(doc: PdfDocument) {

    }
}