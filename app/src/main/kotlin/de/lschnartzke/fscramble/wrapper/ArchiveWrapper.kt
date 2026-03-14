package de.lschnartzke.fscramble.wrapper

import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.archivers.tar.TarFile
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.compress.archivers.zip.ZipFile
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.absolute
import kotlin.io.path.fileSize
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.relativeTo

sealed class ArchiveWrapper : OutputStream() {
    companion object {
        fun getArchiveWrapperByFile(file: File): ArchiveWrapper = when (file.extension) {
            "zip" -> ZipArchiveWrapper(null, ZipArchiveOutputStream(file))
            "tar" -> TarArchiveWrapper(null, TarArchiveOutputStream(file.outputStream()))
            else -> throw IllegalArgumentException("Unsupported file format: $file")
        }

        fun getArchiveWrapperByFile(ifile: File, ofile: File): ArchiveWrapper = when (ofile.extension) {
            "zip" -> ZipArchiveWrapper(ZipFile.Builder().setFile(ifile).get(), ZipArchiveOutputStream(ofile))
            "tar" -> TarArchiveWrapper(TarFile(ifile), TarArchiveOutputStream(ofile.outputStream()))
            else -> throw IllegalArgumentException("Unsupported file format: $ofile")
        }
    }

    class TarArchiveWrapper(val archive: TarFile?, val writer: TarArchiveOutputStream) : ArchiveWrapper() {
        override fun hasNoInputFile(): Boolean = archive == null

        override fun putArchiveEntry(entry: ArchiveEntryWrapper) {
            if (entry !is ArchiveEntryWrapper.TarArchiveEntryWrapper) {
                throw IllegalArgumentException("'entry' should be an instance of 'ArchiveEntryWrapper.TarArchiveEntryWrapper'")
            }

            writer.putArchiveEntry(entry.entry)
        }

        override fun closeArchiveEntry() {
            writer.closeArchiveEntry()
        }

        override val entries: List<ArchiveEntryWrapper>
            get() = archive?.entries?.map { ArchiveEntryWrapper.TarArchiveEntryWrapper(it) } ?: emptyList()

        override fun close() {
            writer.close()
            archive?.close()
        }

        override fun getInputStream(entry: ArchiveEntryWrapper): InputStream {
            if (entry !is ArchiveEntryWrapper.TarArchiveEntryWrapper)
                throw IllegalArgumentException("'entry' should be an instance of 'ArchiveEntryWrapper.TarArchiveEntryWrapper'")

            val archive = archive
            if (archive == null)
                throw IllegalStateException("Can't get input stream of write-only wrapper")

            return archive.getInputStream(entry.entry)
        }

        override fun finish() {
            writer.finish()
        }

        override fun flush() = writer.flush()
        override fun write(p0: Int) = writer.write(p0)
        override fun write(p0: ByteArray) = writer.write(p0)
        override fun write(b: ByteArray?, off: Int, len: Int) = writer.write(b, off, len)
    }

    class ZipArchiveWrapper(val archive: ZipFile?, val writer: ZipArchiveOutputStream) : ArchiveWrapper() {
        override fun hasNoInputFile(): Boolean = archive == null

        override fun putArchiveEntry(entry: ArchiveEntryWrapper) {
            if (entry !is ArchiveEntryWrapper.ZipArchiveEntryWrapper) {
                throw IllegalArgumentException("'entry' should be an instance of 'ArchiveEntryWrapper.ZipArchiveEntryWrapper'")
            }

            writer.putArchiveEntry(entry.entry)
        }

        override fun closeArchiveEntry() {
            writer.closeArchiveEntry()
        }

        override val entries: List<ArchiveEntryWrapper>
            get() = archive?.entries?.toList()?.map { ArchiveEntryWrapper.ZipArchiveEntryWrapper(it) } ?: emptyList()

        override fun close() {
            writer.close()
            archive?.close()
        }

        override fun getInputStream(entry: ArchiveEntryWrapper): InputStream {
            if (entry !is ArchiveEntryWrapper.ZipArchiveEntryWrapper) {
                throw IllegalArgumentException("'entry' should be an instance of 'ArchiveEntryWrapper.ZipArchiveEntryWrapper'")
            }
            val archive = archive
            if (archive == null)
                throw IllegalStateException("Can't get input stream when there's no archive")

            return archive.getInputStream(entry.entry)
        }

        override fun finish() {
            writer.finish()
        }

        override fun flush() = writer.flush()
        override fun write(p0: Int) = writer.write(p0)
        override fun write(p0: ByteArray) = writer.write(p0)
        override fun write(b: ByteArray?, off: Int, len: Int) = writer.write(b, off, len)
    }

    abstract fun getInputStream(entry: ArchiveEntryWrapper): InputStream

    abstract fun hasNoInputFile(): Boolean

    abstract fun putArchiveEntry(entry: ArchiveEntryWrapper)

    abstract fun closeArchiveEntry()

    abstract val entries: List<ArchiveEntryWrapper>

    abstract fun finish()

    fun getArchiveEntryWrapperForFile(ifile: File, forceDir: Boolean = false): ArchiveEntryWrapper {
        val file = if (forceDir && !ifile.name.endsWith("/")) File(ifile, "/") else ifile
        return when (this) {
            is ZipArchiveWrapper -> ArchiveEntryWrapper.ZipArchiveEntryWrapper(
                ZipArchiveEntry(
                    file,
                    file.name
                )
            )

            is TarArchiveWrapper -> ArchiveEntryWrapper.TarArchiveEntryWrapper(
                TarArchiveEntry(
                    file,
                    file.name
                )
            )
        }
    }

    /**
     * Recursively add an entire directory to the archive. Assumes that the entry for the root directory already exists
     * in the archive.
     *
     * @param sourceDir the root directory to add. Used to construct the correct relative paths for the archive.
     * @param currentDir the directory currently being processed. MUST be a subdirectory of sourceDir or equal to it.
     */
    fun addDirectory(sourceDir: Path, currentDir: Path = sourceDir.absolute()) {
        val relativePath = sourceDir.normalize().relativize(currentDir)
        val files = currentDir.listDirectoryEntries()


        for (file in files) {
            val relativeFile = Paths.get("/", sourceDir.relativize(file).toString())
            val forceDir = file.isDirectory()
            val entry = getArchiveEntryWrapperForFile(relativeFile.toFile(), forceDir)
            entry.size = file.fileSize()
            entry.lastModifiedTime = file.getLastModifiedTime()
            putArchiveEntry(entry)
            if (!file.isDirectory()) {
                println("Copying file $file to archive")
                FileUtils.copyFile(file.toFile(), this)
            }
            closeArchiveEntry()
            if (file.isDirectory())
                addDirectory(sourceDir, file.absolute())
        }

    }

}