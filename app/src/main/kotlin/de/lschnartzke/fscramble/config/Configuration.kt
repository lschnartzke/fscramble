package de.lschnartzke.fscramble.config

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

    override fun deserialize(decoder: Decoder): Size {
        val raw = decoder.decodeString()
        val regex = Regex("([0-9]+)\\w*([GMKTgmkt]?)")
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
        val targetDirectory: String,
        val inputDirectory: String,
        val dataDirectory: String,
        val count: Long,
        val create: Boolean = false,
        val createMin: Long = 0,
        val createMax: Long = 0,
    ) : RunConfig()

    @SerialName("create")
    @Serializable
    data class Create(
        val targetDirectory: String,
        val dataDirectory: String,
        val count: Long? = null,
        val size: Size? = null,
        val jobs: Int = Runtime.getRuntime().availableProcessors(),
    ) : RunConfig()
}

@Serializable
data class Configuration(
    val run: HashMap<String, RunConfig>
)
