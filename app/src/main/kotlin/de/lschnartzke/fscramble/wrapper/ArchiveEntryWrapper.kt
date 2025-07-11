package de.lschnartzke.fscramble.wrapper

import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry

sealed class ArchiveEntryWrapper {
    class TarArchiveEntryWrapper(val entry: TarArchiveEntry) : ArchiveEntryWrapper() {
        override val isDirectory: Boolean get() = entry.isDirectory

    }

    class ZipArchiveEntryWrapper(val entry: ZipArchiveEntry) : ArchiveEntryWrapper() {
        override val isDirectory: Boolean
            get() = entry.isDirectory

    }

    abstract val isDirectory: Boolean
}