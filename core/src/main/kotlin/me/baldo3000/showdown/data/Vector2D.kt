package me.baldo3000.showdown.data

import com.badlogic.gdx.math.Vector2
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*

@Serializable(with = Vector2DSerializer::class)
class Vector2D(x: Float = 0f, y: Float = 0f) : Vector2(x, y)

object Vector2DSerializer : KSerializer<Vector2D> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("com.badlogic.gdx.math.Vector2") {
        element<Float>("x")
        element<Float>("y")
    }

    override fun serialize(encoder: Encoder, value: Vector2D) {
        encoder.encodeStructure(descriptor) {
            encodeFloatElement(descriptor, 0, value.x)
            encodeFloatElement(descriptor, 1, value.y)
        }
    }

    override fun deserialize(decoder: Decoder): Vector2D {
        return decoder.decodeStructure(descriptor) {
            var x = 0f
            var y = 0f
            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    CompositeDecoder.DECODE_DONE -> break
                    0 -> x = decodeFloatElement(descriptor, 0)
                    1 -> y = decodeFloatElement(descriptor, 1)
                    else -> throw SerializationException("Unknown index $index")
                }
            }
            Vector2D(x, y)
        }
    }
}
