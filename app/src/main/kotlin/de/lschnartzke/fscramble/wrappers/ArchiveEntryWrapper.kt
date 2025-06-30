package de.lschnartzke.fscramble.wrappers

import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry

sealed class ArchiveEntryWrapper {
    class ZipArchiveEntryWrapper(val entry: ZipArchiveEntry): ArchiveEntryWrapper()
    class TarArchiveEntryWrapper(val tarArchiveEntry: TarArchiveEntry): ArchiveEntryWrapper()
}