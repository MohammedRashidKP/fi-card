package com.closemates.games.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant

@Serializable
data class RoundScore(
    val roundNumber: Int,
    val scores: List<PlayerScore>,
    @Serializable(with = InstantSerializer::class)
    val timestamp: Instant = Instant.now()
)

@Serializable
data class RoundPoint(
    val roundNumber: Int,
    val scores: List<PlayerPoints>,
    @Serializable(with = InstantSerializer::class)
    val timestamp: Instant = Instant.now()
)

@Serializable
data class PlayerPoints(
    val playerId: String,
    val points: Int
)

@Serializable
data class ScoreFile(
    val rounds: MutableList<RoundPoint> = mutableListOf()
)

object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString()) // ISO-8601 format
    }

    override fun deserialize(decoder: Decoder): Instant {
        return Instant.parse(decoder.decodeString())
    }
}