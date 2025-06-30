package de.lschnartzke.fscramble.wrappers

import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.archivers.tar.TarFile
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.compress.archivers.zip.ZipFile

sealed class ArchiveWrapper {
    class ZipWrapper(val archive: ZipFile, val writer: ZipArchiveOutputStream) : ArchiveWrapper() {

    }

    class TarWrapper(val archive: TarFile, val writer: TarArchiveOutputStream) : ArchiveWrapper() {

    }

    abstract fun addEntry


}