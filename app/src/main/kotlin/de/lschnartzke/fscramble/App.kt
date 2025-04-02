package de.lschnartzke.fscramble

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.*
import com.itextpdf.forms.PdfAcroForm
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import org.apache.poi.xwpf.usermodel.ParagraphAlignment
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.usermodel.XWPFParagraph
import java.io.FileOutputStream

/**
 * Scramble one or multiple files or directories
 *
 * needs target and source destinations
 */
class App : CliktCommand() {
    override val printHelpOnEmptyArgs = true
    val dataDirectory: String? by option().help("Directory containing data to use for scrambling (e.g. images to put into PDFs")
    val input: List<String> by option().multiple().help("One or more input files. If the file is a directory, all files in the directory will be scrambled")
    val output: List<String> by option().multiple().help("Where to output scrambeld files or directories. MUST be the same length as input")

    override fun run() {
        val doc = XWPFDocument()
        val paragraph = doc.createParagraph()
        paragraph.alignment = ParagraphAlignment.CENTER
        paragraph.createRun().apply {
            setText("Henlo, wurld")
        }

        doc.write(FileOutputStream("out.docx"))
    }
}

fun main(args: Array<String>) {
    App().main(args)
}
