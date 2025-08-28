package de.lschnartzke.fscramble.wrapper

import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import java.nio.file.attribute.FileTime
import java.util.Date

sealed class ArchiveEntryWrapper {
    class TarArchiveEntryWrapper(val entry: TarArchiveEntry) : ArchiveEntryWrapper() {
        override val isDirectory: Boolean get() = entry.isDirectory

        override var size: Long = entry.size
        override var lastModifiedTime: FileTime = entry.lastModifiedTime
    }

    class ZipArchiveEntryWrapper(val entry: ZipArchiveEntry) : ArchiveEntryWrapper() {
        override val isDirectory: Boolean
            get() = entry.isDirectory


        override var size: Long = entry.size
        override var lastModifiedTime: FileTime = entry.lastModifiedTime
    }

    abstract var size: Long
    abstract var lastModifiedTime: FileTime
    abstract val isDirectory: Boolean
}