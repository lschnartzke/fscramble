package de.lschnartzke.fscramble.scramblers

import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
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

    private fun scrambleAddPage(doc: PdfDocument) {

    }

    private fun scrambleRemovePage(doc: PdfDocument) {

    }

    private fun scrambleAddText(doc: PdfDocument) {

    }

    private fun scrambleRemoveText(doc: PdfDocument) {

    }

    private fun scrambleAddMedia(doc: PdfDocument) {

    }

    private fun scrambleRemoveMedia(doc: PdfDocument) {

    }
}