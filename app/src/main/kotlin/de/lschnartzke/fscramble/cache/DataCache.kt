package de.lschnartzke.fscramble.cache

import io.klogging.logger
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.InputStreamReader
import kotlin.random.Random
import kotlin.system.exitProcess

/**
 * The DataCache is responsible for caching data used during the scrambling process.
 * It simply  load all the provided data files into memory and tries to preprocess them as much as possible
 */
class DataCache private constructor() {
    companion object {
        private lateinit var instance: DataCache
        fun getDataCache(): DataCache = instance

        fun init(dataDirectory: String) {
            instance = DataCache().apply{ runBlocking { init(dataDirectory) } }
        }
    }

    data class ImageData(val file: File, val content: ByteArray) {
        // generated because the IDE told me to (pre-AI Vibe-Coding)
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ImageData

            if (file != other.file) return false
            if (!content.contentEquals(other.content)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = file.hashCode()
            result = 31 * result + content.contentHashCode()
            return result
        }
    }

    private val logger = logger<DataCache>()

    private val textData: MutableList<String> = mutableListOf()
    private val imageData: MutableList<ImageData> = mutableListOf()

    private suspend fun loadTextFile(file: File) {
        logger.info("Loading text file", "file" to file.path)
        val reader = file.bufferedReader()

        var count = 0
        val builder = StringBuilder()
        for (line in reader.readLines()) {
            if (line.trim().isEmpty() && builder.isNotEmpty()) {
                textData.add(builder.toString())
                builder.clear()
                count += 1
                continue
            } else if (line.trim().isEmpty()) {
                continue
            }

            builder.append(line)
            builder.append('\n')
        }

        // If the file does not end with a blank line, the last paragraph would be lost
        if (builder.isNotEmpty()) {
            textData.add(builder.toString())
            count += 1
        }

        logger.info("Lines loaded from file", "count" to count, "file" to file.path)
    }

    private suspend fun loadImageFile(file: File) {
        logger.info("Loading image file", "file" to file.path)
        val bytes = file.readBytes()

        val data = ImageData(file, bytes)
        imageData.add(data)
    }

    private suspend fun init(dataDirectory: String) {
        val dir = File(dataDirectory)
        logger.info("Initializing DataCache", "dataDirectory" to dataDirectory)
        if (!dir.isDirectory) {
            logger.fatal("Not a directory", "dataDirectory" to dataDirectory)
            exitProcess(1)
        }

        val files = dir.listFiles() ?: return
        for (file in files) {
            when (file.extension) {
                "txt" -> loadTextFile(file)
                "png", "jpeg", "jpg", "gif" -> loadImageFile(file)
            }
        }
    }

    fun getRandomImageData(): ImageData? {
        if (imageData.isEmpty())
            return null

        return imageData[Random(System.nanoTime()).nextInt(until = imageData.size)]
    }

    fun getRandomParagraph(): String {
        if (textData.isEmpty()) {
            return "" // TODO: Return randomly generated string
        }

        return textData[Random(System.nanoTime()).nextInt(until = textData.size)]
    }
}