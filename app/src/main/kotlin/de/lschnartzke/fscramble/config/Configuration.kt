package de.lschnartzke.fscramble.config

import de.lschnartzke.fscramble.scramblers.AbstractArchiveScrambler
import de.lschnartzke.fscramble.scramblers.AbstractScrambler
import io.klogging.logger
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = SizeSerializer::class)
data class Size(val size: Long? = null)

object SizeSerializer : KSerializer<Size> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(SizeSerializer::class.qualifiedName!!, PrimitiveKind.STRING)
    private val logger = logger<SizeSerializer>()

    override fun deserialize(decoder: Decoder): Size {
        val raw = decoder.decodeString()
        val regex = Regex("([0-9]+)\\s*([GMKTgmkt]?)")
        val res = regex.find(raw)
        if (res == null) {
            return Size(null)
        }

        assert (res.groupValues.size == 3)

        val base = res.groupValues[1].toLong()
        val unit = res.groupValues[2]

        val size = when (unit.lowercase()) {
            "k" -> base * 1024
            "m" -> base * 1024 * 1024
            "g" -> base * 1024 * 1024 * 1024
            "t" -> base * 1024 * 1024 * 1024 * 1024
            else -> base
        }

        return Size(size)
    }

    override fun serialize(encoder: Encoder, value: Size) {
         val size = value.size
        if (size != null) {
            encoder.encodeLong(size)
        }
    }
}

@Serializable
sealed class RunConfig {
    @SerialName("scramble")
    @Serializable
    data class Scramble(
        // where to store scrambled files
        val targetDirectory: String,
        // where to source files to scramble from
        val inputDirectory: String,
        // where to source content from to use during scrambling
        val dataDirectory: String,
        // if true, also create archives
        val archives: Boolean = false,
        // what kinds of archives to create
        val archiveTypes: List<String> = AbstractArchiveScrambler.supportedArchives,
        // how many scramble actions to perform per file
        val count: Long,
        // if true, also create new files
        val create: Boolean = false,
        // if create, how many files to create at least
        val createMin: Long = 0,
        // if create, how many files to create at most
        val createMax: Long = 0,
    ) : RunConfig()

    @SerialName("create")
    @Serializable
    data class Create(
        // where to store generated files
        val targetDirectory: String,
        // where to source content for generated files from
        val dataDirectory: String,
        // if true, also create archives
        val archives: Boolean = false,
        // arbitrary files to use for creating archives
        val inputDirectory: String? = null,
        // List of file types to create
        val fileTypes: List<String> = AbstractScrambler.supportedExtensions,
        // what type of archives to create
        val archiveTypes: List<String> = AbstractArchiveScrambler.supportedArchives,
        // how many files to create (conflicts with size)
        val count: Long? = null,
        // estimated amount of data to generate (conflicts with count)
        val size: Size? = null,
        // how many create-processes to run in parallel (only relevant for size)
        val jobs: Int = Runtime.getRuntime().availableProcessors(),
    ) : RunConfig()
}

@Serializable
data class Configuration(
    val run: HashMap<String, RunConfig>
)
